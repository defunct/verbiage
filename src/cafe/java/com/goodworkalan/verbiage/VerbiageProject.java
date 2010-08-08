package com.goodworkalan.verbiage;

import com.goodworkalan.cafe.ProjectModule;
import com.goodworkalan.cafe.builder.Builder;
import com.goodworkalan.cafe.outline.JavaProject;

/**
 * Builds the project definition for Verbiage.
 *
 * @author Alan Gutierrez
 */
public class VerbiageProject implements ProjectModule {
    /**
     * Build the project definition for Verbiage.
     *
     * @param builder
     *          The project builder.
     */
    public void build(Builder builder) {
        builder
            .cookbook(JavaProject.class)
                .produces("com.github.bigeasy.verbiage/verbiage/0.1.0.10")
                .depends()
                    .development("org.testng/testng-jdk15/5.10")
                    .end()
                .end()
            .end();
    }
}
