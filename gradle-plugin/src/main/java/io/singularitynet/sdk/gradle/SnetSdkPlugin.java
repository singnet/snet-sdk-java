package io.singularitynet.sdk.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class SnetSdkPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().create("getSingularityNetServiceApi",
                GetSingularityNetServiceApi.class, (task) -> {});
    }

}
