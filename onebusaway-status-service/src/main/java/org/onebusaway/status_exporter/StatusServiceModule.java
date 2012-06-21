/**
 * Copyright (C) 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.status_exporter;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class StatusServiceModule extends AbstractModule {

  public static void addModuleAndDependencies(Set<Module> modules) {
    modules.add(new StatusServiceModule());
  }

  @Override
  protected void configure() {

    bind(StatusService.class);

    final Set<StatusProviderService> providers = new HashSet<StatusProviderService>();
    bind(new TypeLiteral<Set<StatusProviderService>>() {
    }).annotatedWith(Names.named(StatusService.PROVIDERS_NAME)).toInstance(
        providers);

    /**
     * Collect all the StatusProviderService instances as they are instantiated.
     */
    bindListener(Matchers.any(), new TypeListener() {
      @Override
      public <I> void hear(TypeLiteral<I> injectableType,
          TypeEncounter<I> encounter) {

        Class<? super I> type = injectableType.getRawType();
        if (StatusProviderService.class.isAssignableFrom(type)) {
          encounter.register(new InjectionListenerImpl<I>(providers));
        }
      }
    });
  }

  /**
   * Implement hashCode() and equals() such that two instances of the module
   * will be equal.
   */
  @Override
  public int hashCode() {
    return this.getClass().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null)
      return false;
    return this.getClass().equals(o.getClass());
  }

  private static class InjectionListenerImpl<I> implements InjectionListener<I> {

    private final Set<StatusProviderService> _providers;

    public InjectionListenerImpl(Set<StatusProviderService> providers) {
      _providers = providers;
    }

    @Override
    public void afterInjection(I injectee) {
      if (injectee instanceof StatusProviderService) {
        _providers.add((StatusProviderService) injectee);
      }
    }
  }
}