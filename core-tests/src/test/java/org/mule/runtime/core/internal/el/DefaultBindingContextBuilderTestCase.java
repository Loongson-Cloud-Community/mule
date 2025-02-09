/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.el;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.metadata.DataType.OBJECT;
import static org.mule.runtime.api.metadata.DataType.STRING;

import org.mule.runtime.api.el.Binding;
import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.el.ExpressionModule;
import org.mule.runtime.api.el.ModuleNamespace;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.LazyValue;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.util.Collection;
import java.util.Map;

import org.junit.Test;

public class DefaultBindingContextBuilderTestCase extends AbstractMuleTestCase {

  private static final String ID = "id";
  private static final String OTHER_ID = "otherId";

  private final BindingContext.Builder builder = new DefaultBindingContextBuilder();
  private final TypedValue<String> typedValue = new TypedValue<>("", STRING);
  private final ModuleNamespace namespace = new ModuleNamespace("org", "mule", "mymodule");

  @Test
  public void addsBinding() {
    BindingContext context = builder.addBinding(ID, typedValue).build();

    assertThat(context.bindings(), hasSize(1));
    assertThat(context.identifiers(), hasItem("id"));
    assertThat(context.lookup("id").get(), is(sameInstance(typedValue)));
  }

  @Test
  public void addsBindings() {
    ExpressionModule module =
        ExpressionModule.builder(namespace).addBinding(ID, typedValue).build();
    BindingContext previousContext =
        BindingContext.builder()
            .addBinding(ID, typedValue)
            .addBinding(OTHER_ID, typedValue)
            .addModule(module)
            .build();


    BindingContext context = builder.addAll(previousContext).build();

    assertThat(context.bindings(), hasSize(2));
    assertThat(context.identifiers(), hasItems(ID, OTHER_ID));
    assertThat(context.lookup(ID).get(), is(sameInstance(typedValue)));
    assertThat(context.lookup(OTHER_ID).get(), is(sameInstance(typedValue)));

    assertThat(context.modules(), hasSize(1));
    assertThat(context.modules(), hasItems(module));
    Collection<Binding> moduleBindings = context.modules().iterator().next().bindings();
    assertThat(moduleBindings, hasSize(1));
    assertThat(moduleBindings.iterator().next().identifier(), is(ID));
  }


  @Test
  public void fromPreviousBindings() {
    ExpressionModule module = ExpressionModule.builder(namespace).addBinding("id", typedValue).build();
    BindingContext previousContext =
        BindingContext.builder()
            .addBinding(ID, typedValue)
            .addBinding(OTHER_ID, typedValue)
            .addModule(module)
            .build();

    BindingContext context = BindingContext.builder(previousContext).build();

    assertThat(context.bindings(), hasSize(2));
    assertThat(context.identifiers(), hasItems(ID, OTHER_ID));
    assertThat(context.lookup(ID).get(), is(sameInstance(typedValue)));
    assertThat(context.lookup(OTHER_ID).get(), is(sameInstance(typedValue)));

    assertThat(context.modules(), hasSize(1));
    assertThat(context.modules(), hasItems(module));
  }

  @Test
  public void payloadLookupInFirstDelegate() {
    BindingContext localBinding = BindingContext.builder()
        .addBinding("payload", mapValue())
        .build();
    assertLookup(BindingContext.builder(localBinding).addAll(globalBinding()).build(), "payload");
  }

  @Test
  public void payloadLookupInSecondDelegate() {
    BindingContext localBinding = BindingContext.builder()
        .addBinding("payload", new LazyValue<>(() -> mapValue()))
        .build();
    assertLookup(BindingContext.builder(globalBinding()).addAll(localBinding).build(), "payload");
  }

  @Test
  public void attributesLookupInFirstDelegate() {
    BindingContext localBinding = BindingContext.builder()
        .addBinding("attributes", mapValue())
        .build();
    assertLookup(BindingContext.builder(localBinding).addAll(globalBinding()).build(), "attributes");
  }

  @Test
  public void attributesLookupInSecondDelegate() {
    BindingContext localBinding = BindingContext.builder()
        .addBinding("attributes", new LazyValue<>(() -> mapValue()))
        .build();
    assertLookup(BindingContext.builder(globalBinding()).addAll(localBinding).build(), "attributes");
  }

  @Test
  public void varLookupInFirstDelegate() {
    BindingContext localBinding = BindingContext.builder()
        .addBinding("vars", new LazyValue<>(() -> mapValue()))
        .build();
    assertLookup(BindingContext.builder(localBinding).addAll(globalBinding()).build(), "vars");
  }

  @Test
  public void varLookupInSecondDelegate() {
    BindingContext localBinding = BindingContext.builder()
        .addBinding("vars", new LazyValue<>(() -> mapValue()))
        .build();
    assertLookup(BindingContext.builder(globalBinding()).addAll(localBinding).build(), "vars");
  }

  @Test
  public void miscLazyLookupInFirstDelegate() {
    BindingContext localBinding = BindingContext.builder()
        .addBinding("misc", mapValue())
        .build();
    assertLookup(BindingContext.builder(localBinding).addAll(globalBinding()).build(), "misc");
  }

  @Test
  public void miscLazyLookupInSecondDelegate() {
    BindingContext localBinding = BindingContext.builder()
        .addBinding("misc", new LazyValue<>(() -> mapValue()))
        .build();
    assertLookup(BindingContext.builder(globalBinding()).addAll(localBinding).build(), "misc");
  }

  @Test
  public void miscLookupInFirstDelegate() {
    BindingContext localBinding = BindingContext.builder()
        .addBinding("misc", mapValue())
        .build();
    assertLookup(BindingContext.builder(localBinding).addAll(globalBinding()).build(), "misc");
  }

  @Test
  public void miscLookupInSecondDelegate() {
    BindingContext localBinding = BindingContext.builder()
        .addBinding("misc", new LazyValue<>(() -> mapValue()))
        .build();
    assertLookup(BindingContext.builder(globalBinding()).addAll(localBinding).build(), "misc");
  }

  private TypedValue<Map<String, TypedValue<String>>> mapValue() {
    return new TypedValue<>(singletonMap("key", new TypedValue<>("value", STRING)), OBJECT);
  }

  private BindingContext globalBinding() {
    return BindingContext.builder().addBinding("g", new TypedValue<>("gValue", STRING)).build();
  }

  private void assertLookup(BindingContext composite, String identifier) {
    TypedValue vars = composite.lookup(identifier).get();
    assertThat(((Map<String, TypedValue<String>>) vars.getValue()).get("key").getValue(), is("value"));
    assertThat(composite.lookup("g").get().getValue(), is("gValue"));
  }

}
