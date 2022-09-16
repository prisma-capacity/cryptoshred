#!/usr/bin/env kotlin

@file:DependsOn("it.krzeminski:github-actions-kotlin-dsl:0.27.0")

import it.krzeminski.githubactions.actions.CustomAction
import it.krzeminski.githubactions.actions.actions.CheckoutV3
import it.krzeminski.githubactions.actions.actions.SetupJavaV3
import it.krzeminski.githubactions.domain.RunnerType
import it.krzeminski.githubactions.domain.Workflow
import it.krzeminski.githubactions.domain.triggers.Push
import it.krzeminski.githubactions.dsl.workflow
import it.krzeminski.githubactions.yaml.writeToFile
import java.nio.file.Paths

public val workflowFormat: Workflow = workflow(
      name = "Format",
      on = listOf(
        Push(),
        ),
      sourceFile = Paths.get(".github/kts/format.main.kts"),
    ) {
      job(
        id = "formatting",
        runsOn = RunnerType.UbuntuLatest,
      ) {
        uses(
          name = "Checkout",
          action = CheckoutV3(
            fetchDepth = CheckoutV3.FetchDepth.Infinite,
          ),
        )
        uses(
          name = "SetupJava",
          action = SetupJavaV3(
            javaVersion = "17",
            distribution = SetupJavaV3.Distribution.Corretto,
          ),
        )
        uses(
          name = "GooglejavaformatActionV3",
          action = CustomAction(
            actionOwner = "axel-op",
            actionName = "googlejavaformat-action",
            actionVersion = "v3",
            inputs = mapOf(
              "args" to " --skip-javadoc-formatting --skip-reflowing-long-strings --skip-sorting-imports --replace",
              "version" to "1.15",
            )
          ),
        )
      }

    }

workflowFormat.writeToFile(addConsistencyCheck = false)
