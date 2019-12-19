package io.singularitynet.sdk.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class GetSingularityNetServiceApi extends DefaultTask {

    @TaskAction
    void getSingularityNetServiceApi() {
        System.out.println("Hello");
    }

}
