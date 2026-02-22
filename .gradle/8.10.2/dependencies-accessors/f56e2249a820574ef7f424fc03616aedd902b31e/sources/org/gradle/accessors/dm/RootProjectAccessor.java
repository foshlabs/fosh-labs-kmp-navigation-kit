package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.internal.artifacts.dependencies.ProjectDependencyInternal;
import org.gradle.api.internal.artifacts.DefaultProjectDependencyFactory;
import org.gradle.api.internal.artifacts.dsl.dependencies.ProjectFinder;
import org.gradle.api.internal.catalog.DelegatingProjectDependency;
import org.gradle.api.internal.catalog.TypeSafeProjectDependencyFactory;
import javax.inject.Inject;

@NonNullApi
public class RootProjectAccessor extends TypeSafeProjectDependencyFactory {


    @Inject
    public RootProjectAccessor(DefaultProjectDependencyFactory factory, ProjectFinder finder) {
        super(factory, finder);
    }

    /**
     * Creates a project dependency on the project at path ":"
     */
    public FoshLabsKmpNavigationKitProjectDependency getFoshLabsKmpNavigationKit() { return new FoshLabsKmpNavigationKitProjectDependency(getFactory(), create(":")); }

    /**
     * Creates a project dependency on the project at path ":navigation-compose"
     */
    public NavigationComposeProjectDependency getNavigationCompose() { return new NavigationComposeProjectDependency(getFactory(), create(":navigation-compose")); }

    /**
     * Creates a project dependency on the project at path ":navigation-core"
     */
    public NavigationCoreProjectDependency getNavigationCore() { return new NavigationCoreProjectDependency(getFactory(), create(":navigation-core")); }

}
