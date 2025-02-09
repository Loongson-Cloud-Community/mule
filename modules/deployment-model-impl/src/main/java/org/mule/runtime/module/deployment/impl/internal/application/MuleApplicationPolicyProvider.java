/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.deployment.impl.internal.application;

import static java.lang.Integer.compare;
import static java.lang.String.format;
import static java.util.Optional.of;
import static org.mule.runtime.api.util.Preconditions.checkArgument;
import static org.mule.runtime.http.policy.api.SourcePolicyAwareAttributes.noAttributes;

import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.core.api.policy.Policy;
import org.mule.runtime.core.api.policy.PolicyParametrization;
import org.mule.runtime.core.api.policy.PolicyProvider;
import org.mule.runtime.deployment.model.api.application.Application;
import org.mule.runtime.deployment.model.api.policy.PolicyRegistrationException;
import org.mule.runtime.deployment.model.api.policy.PolicyTemplate;
import org.mule.runtime.deployment.model.api.policy.PolicyTemplateDescriptor;
import org.mule.runtime.module.deployment.impl.internal.policy.ApplicationPolicyInstance;
import org.mule.runtime.module.deployment.impl.internal.policy.PolicyInstanceProviderFactory;
import org.mule.runtime.module.deployment.impl.internal.policy.PolicyTemplateFactory;
import org.mule.runtime.policy.api.AttributeAwarePointcut;
import org.mule.runtime.policy.api.PolicyAwareAttributes;
import org.mule.runtime.policy.api.PolicyPointcutParameters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Provides policy management and provision for Mule applications
 */
public class MuleApplicationPolicyProvider implements ApplicationPolicyProvider, PolicyProvider, Disposable {

  public static final String IS_POLICY_REORDER = "isPolicyReorder";

  private final PolicyTemplateFactory policyTemplateFactory;
  private final PolicyInstanceProviderFactory policyInstanceProviderFactory;
  private final List<RegisteredPolicyTemplate> registeredPolicyTemplates = new LinkedList<>();
  private final List<RegisteredPolicyInstanceProvider> registeredPolicyInstanceProviders = new LinkedList<>();
  private PolicyAwareAttributes sourcePolicyAwareAttributes = noAttributes();
  private Application application;

  private Runnable policiesChangedCallback = () -> {
  };

  /**
   * Creates a new provider
   *
   * @param policyTemplateFactory         used to create the policy templates for the application. Non null.
   * @param policyInstanceProviderFactory used to create the policy instances for the application. Non null.
   */
  public MuleApplicationPolicyProvider(PolicyTemplateFactory policyTemplateFactory,
                                       PolicyInstanceProviderFactory policyInstanceProviderFactory) {
    this.policyTemplateFactory = policyTemplateFactory;
    this.policyInstanceProviderFactory = policyInstanceProviderFactory;
  }

  @Override
  public synchronized void addPolicy(PolicyTemplateDescriptor policyTemplateDescriptor, PolicyParametrization parametrization)
      throws PolicyRegistrationException {
    try {
      checkArgument(application != null, "application was not configured on the policy provider");

      Optional<RegisteredPolicyInstanceProvider> registeredPolicyInstanceProvider = registeredPolicyInstanceProviders.stream()
          .filter(p -> p.getPolicyId().equals(parametrization.getId())).findFirst();

      // Check if policy is already applied.
      if (registeredPolicyInstanceProvider.isPresent()) {
        // TODO MULE-19415 - Expose Api For policy reordering.
        // Check if incoming parameters indicate that the operation is a policy reorder.
        if (isPolicyReorder(parametrization)) {
          reorderPolicy(registeredPolicyInstanceProvider.get(), parametrization);
          return;
        } else {
          throw new IllegalArgumentException(createPolicyAlreadyRegisteredError(parametrization.getId()));
        }
      }

      Optional<RegisteredPolicyTemplate> registeredPolicyTemplate = registeredPolicyTemplates.stream()
          .filter(p -> p.policyTemplate.getDescriptor().getBundleDescriptor().getGroupId()
              .equals(policyTemplateDescriptor.getBundleDescriptor().getGroupId()) &&
              p.policyTemplate.getDescriptor().getBundleDescriptor().getArtifactId()
                  .equals(policyTemplateDescriptor.getBundleDescriptor().getArtifactId())
              &&
              p.policyTemplate.getDescriptor().getBundleDescriptor().getVersion()
                  .equals(policyTemplateDescriptor.getBundleDescriptor().getVersion()))
          .findAny();

      if (!registeredPolicyTemplate.isPresent()) {
        PolicyTemplate policyTemplate = policyTemplateFactory.createArtifact(application, policyTemplateDescriptor);
        registeredPolicyTemplate = of(new RegisteredPolicyTemplate(policyTemplate));
        registeredPolicyTemplates.add(registeredPolicyTemplate.get());
      }

      ApplicationPolicyInstance applicationPolicyInstance = policyInstanceProviderFactory
          .create(application, registeredPolicyTemplate.get().policyTemplate, parametrization);

      applicationPolicyInstance.initialise();

      registeredPolicyInstanceProviders
          .add(new RegisteredPolicyInstanceProvider(applicationPolicyInstance, parametrization.getId()));
      registeredPolicyTemplate.get().count++;
      sortPolicies();

    } catch (Exception e) {
      throw new PolicyRegistrationException(createPolicyRegistrationError(parametrization.getId()), e);
    }
  }

  /**
   * Check if ApiGateway sent
   *
   * @param parametrization
   * @return true if the parametrization corresponds to a policy reorder. false otherwise.
   */
  private boolean isPolicyReorder(PolicyParametrization parametrization) {
    return parametrization.getParameters().getOrDefault(IS_POLICY_REORDER, "false").equalsIgnoreCase("true");
  }

  private void reorderPolicy(RegisteredPolicyInstanceProvider provider, PolicyParametrization parametrization) {
    provider.updateOrder(parametrization.getOrder());
    sortPolicies();
  }

  /**
   * Sorts internal policy list.
   */
  private void sortPolicies() {
    registeredPolicyInstanceProviders.sort(null);
    policiesChangedCallback.run();
  }

  @Override
  public synchronized boolean removePolicy(String parametrizedPolicyId) {
    Optional<RegisteredPolicyInstanceProvider> registeredPolicyInstanceProvider = registeredPolicyInstanceProviders.stream()
        .filter(p -> p.getPolicyId().equals(parametrizedPolicyId)).findFirst();

    registeredPolicyInstanceProvider.ifPresent(provider -> {

      registeredPolicyInstanceProviders.remove(provider);

      // Run callback before disposing the policy to be able to dispose Composite Policies before policy schedulers are shutdown
      policiesChangedCallback.run();

      provider.getApplicationPolicyInstance().dispose();

      Optional<RegisteredPolicyTemplate> registeredPolicyTemplate = registeredPolicyTemplates.stream()
          .filter(p -> p.policyTemplate.equals(provider.getApplicationPolicyInstance().getPolicyTemplate()))
          .findFirst();

      if (!registeredPolicyTemplate.isPresent()) {
        throw new IllegalStateException("Cannot find registered policy template");
      }

      registeredPolicyTemplate.get().count--;
      if (registeredPolicyTemplate.get().count == 0) {
        application.getRegionClassLoader()
            .removeClassLoader(registeredPolicyTemplate.get().policyTemplate.getArtifactClassLoader());
        registeredPolicyTemplate.get().policyTemplate.dispose();
        registeredPolicyTemplates.remove(registeredPolicyTemplate.get());
      }
    });

    return registeredPolicyInstanceProvider.isPresent();
  }

  @Override
  public synchronized boolean isPoliciesAvailable() {
    return !registeredPolicyInstanceProviders.isEmpty();
  }

  @Override
  public boolean isSourcePoliciesAvailable() {
    return registeredPolicyInstanceProviders
        .stream()
        .anyMatch(pip -> pip.getApplicationPolicyInstance().getSourcePolicy().isPresent());
  }

  @Override
  public boolean isOperationPoliciesAvailable() {
    return registeredPolicyInstanceProviders
        .stream()
        .anyMatch(pip -> pip.getApplicationPolicyInstance().getOperationPolicy().isPresent());
  }

  @Override
  public void onPoliciesChanged(Runnable policiesChangedCallback) {
    this.policiesChangedCallback = () -> {
      policiesChangedCallback.run();
      updatePolicyAwareAttributes();
    };
  }

  private synchronized void updatePolicyAwareAttributes() {
    sourcePolicyAwareAttributes = registeredPolicyInstanceProviders.stream()
        .filter(pip -> pip.getApplicationPolicyInstance().getPointcut() instanceof AttributeAwarePointcut)
        .map(pip -> ((AttributeAwarePointcut) pip.getApplicationPolicyInstance().getPointcut()).sourcePolicyAwareAttributes())
        .reduce(noAttributes(), PolicyAwareAttributes::merge);
  }

  @Override
  public List<Policy> findSourceParameterizedPolicies(PolicyPointcutParameters policyPointcutParameters) {
    List<Policy> policies = new ArrayList<>();

    if (!registeredPolicyInstanceProviders.isEmpty()) {
      for (RegisteredPolicyInstanceProvider registeredPolicyInstanceProvider : registeredPolicyInstanceProviders) {
        if (registeredPolicyInstanceProvider.getApplicationPolicyInstance().getPointcut().matches(policyPointcutParameters)) {
          if (registeredPolicyInstanceProvider.getApplicationPolicyInstance().getSourcePolicy().isPresent()) {
            policies.add(registeredPolicyInstanceProvider.getApplicationPolicyInstance().getSourcePolicy().get());
          }
        }
      }
    }

    return policies;
  }

  @Override
  public synchronized PolicyAwareAttributes sourcePolicyAwareAttributes() {
    return sourcePolicyAwareAttributes;
  }

  @Override
  public List<Policy> findOperationParameterizedPolicies(PolicyPointcutParameters policyPointcutParameters) {
    List<Policy> policies = new ArrayList<>();

    if (!registeredPolicyInstanceProviders.isEmpty()) {
      for (RegisteredPolicyInstanceProvider registeredPolicyInstanceProvider : registeredPolicyInstanceProviders) {
        if (registeredPolicyInstanceProvider.getApplicationPolicyInstance().getPointcut().matches(policyPointcutParameters)) {
          if (registeredPolicyInstanceProvider.getApplicationPolicyInstance().getOperationPolicy().isPresent()) {
            policies.add(registeredPolicyInstanceProvider.getApplicationPolicyInstance().getOperationPolicy().get());
          }
        }
      }
    }

    return policies;
  }

  @Override
  public void dispose() {

    for (RegisteredPolicyInstanceProvider registeredPolicyInstanceProvider : registeredPolicyInstanceProviders) {
      registeredPolicyInstanceProvider.getApplicationPolicyInstance().dispose();
    }
    registeredPolicyInstanceProviders.clear();

    for (RegisteredPolicyTemplate registeredPolicyTemplate : registeredPolicyTemplates) {
      try {
        registeredPolicyTemplate.policyTemplate.dispose();
      } catch (RuntimeException e) {
        // Ignore and continue
      }


      registeredPolicyTemplates.clear();
    }
  }

  public void setApplication(Application application) {
    this.application = application;
  }

  static String createPolicyAlreadyRegisteredError(String policyId) {
    return format("Policy already registered: '%s'", policyId);
  }

  static String createPolicyRegistrationError(String policyId) {
    return format("Error occured registering policy '%s'", policyId);
  }

  private static class RegisteredPolicyTemplate {

    private volatile int count;
    private final PolicyTemplate policyTemplate;

    private RegisteredPolicyTemplate(PolicyTemplate policyTemplate) {
      this.policyTemplate = policyTemplate;
    }
  }

  private static class RegisteredPolicyInstanceProvider implements Comparable<RegisteredPolicyInstanceProvider> {

    private final ApplicationPolicyInstance applicationPolicyInstance;
    private final String policyId;
    private int order;

    public RegisteredPolicyInstanceProvider(ApplicationPolicyInstance applicationPolicyInstance, String policyId) {
      this.applicationPolicyInstance = applicationPolicyInstance;
      this.policyId = policyId;
      this.order = applicationPolicyInstance.getOrder();
    }

    public void updateOrder(int newOrder) {
      this.order = newOrder;
    }

    @Override
    public int compareTo(RegisteredPolicyInstanceProvider registeredPolicyInstanceProvider) {
      return compare(order, registeredPolicyInstanceProvider.order);
    }

    public ApplicationPolicyInstance getApplicationPolicyInstance() {
      return applicationPolicyInstance;
    }

    public String getPolicyId() {
      return policyId;
    }
  }
}
