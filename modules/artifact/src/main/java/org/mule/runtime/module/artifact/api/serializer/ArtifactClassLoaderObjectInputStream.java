/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.module.artifact.api.serializer;

import static java.lang.Class.forName;

import org.mule.api.annotation.NoInstantiate;
import org.mule.runtime.module.artifact.api.classloader.ClassLoaderRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Customized version of {@link ObjectInputStream} that reads the identifier of the class loader that loaded the class of the
 * serialized object.
 * <p>
 * Is intended to be used along with {@link ArtifactClassLoaderObjectOutputStream}.
 */
@NoInstantiate
public final class ArtifactClassLoaderObjectInputStream extends ObjectInputStream {

  private final ClassLoaderRepository classLoaderRepository;

  /**
   * Creates a new stream instance.
   *
   * @param classLoaderRepository contains the registered classloaders that can be used to load serialized classes. Non null.
   * @param input                 input stream to read from. Non null.
   * @throws IOException if an I/O error occurs while reading stream header
   */
  public ArtifactClassLoaderObjectInputStream(ClassLoaderRepository classLoaderRepository, InputStream input)
      throws IOException {
    super(input);
    this.classLoaderRepository = classLoaderRepository;
  }

  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
    int val = readInt();
    if (val == -1) {
      return super.resolveClass(desc);
    }

    byte[] bytes = new byte[val];
    readFully(bytes);

    String classLoaderId = new String(bytes);
    ClassLoader classLoader = classLoaderRepository.find(classLoaderId)
        .orElseThrow(() -> new IOException("Artifact class loader not found: " + classLoaderId));

    return forName(desc.getName(), false, classLoader);
  }
}
