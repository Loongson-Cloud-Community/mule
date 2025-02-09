/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.util;

import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.privileged.store.DeserializationPostInitialisable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.apache.commons.lang3.SerializationException;

public class SerializationUtils {

  public static Object deserialize(InputStream inputStream, MuleContext muleContext) {
    if (muleContext == null) {
      throw new IllegalArgumentException("The MuleContext must not be null");
    }
    return deserialize(inputStream, muleContext.getExecutionClassLoader(), muleContext);
  }

  public static Object deserialize(byte[] objectData, MuleContext muleContext) {
    if (muleContext == null) {
      throw new IllegalArgumentException("The MuleContext must not be null");
    }
    return deserialize(objectData, muleContext.getExecutionClassLoader(), muleContext);
  }

  /**
   * <p>
   * Deserializes an <code>Object</code> from the specified stream.
   * </p>
   * <p/>
   * <p>
   * The stream will be closed once the object is written. This avoids the need for a finally clause, and maybe also exception
   * handling, in the application code.
   * </p>
   * <p/>
   * <p>
   * The stream passed in is not buffered internally within this method. This is the responsibility of your application if
   * desired.
   * </p>
   *
   * @param inputStream the serialized object input stream, must not be null
   * @param cl          classloader which can load custom classes from the stream
   * @return the deserialized object
   * @throws IllegalArgumentException                        if <code>inputStream</code> is <code>null</code>
   * @throws org.apache.commons.lang3.SerializationException (runtime) if the serialization fails
   */
  public static Object deserialize(InputStream inputStream, ClassLoader cl, MuleContext muleContext) {
    if (inputStream == null) {
      throw new IllegalArgumentException("The InputStream must not be null");
    }
    if (cl == null) {
      throw new IllegalArgumentException("The ClassLoader must not be null");
    }
    ObjectInputStream in = null;
    try {
      // stream closed in the finally
      in = new ClassLoaderObjectInputStream(cl, inputStream);
      Object obj = in.readObject();
      if (obj instanceof DeserializationPostInitialisable) {
        DeserializationPostInitialisable.Implementation.init(obj, muleContext);
      }
      return obj;
    } catch (ClassNotFoundException ex) {
      throw new SerializationException(ex);
    } catch (IOException ex) {
      throw new SerializationException(ex);
    } catch (Exception ex) {
      throw new SerializationException(ex);
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException ex) {
        // ignore close exception
      }
    }
  }

  /**
   * <p>
   * Deserializes a single <code>Object</code> from an array of bytes.
   * </p>
   *
   * @param objectData the serialized object, must not be null
   * @param cl         classloader which can load custom classes from the stream
   * @return the deserialized object
   * @throws IllegalArgumentException if <code>objectData</code> is <code>null</code>
   * @throws SerializationException   (runtime) if the serialization fails
   */
  private static Object deserialize(byte[] objectData, ClassLoader cl, MuleContext muleContext) {
    if (objectData == null) {
      throw new IllegalArgumentException("The byte[] must not be null");
    }
    ByteArrayInputStream bais = new ByteArrayInputStream(objectData);
    return deserialize(bais, cl, muleContext);
  }
}
