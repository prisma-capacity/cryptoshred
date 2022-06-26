#!/usr/bin/env kotlin

@file:DependsOn("it.krzeminski:github-actions-kotlin-dsl:0.20.0")

import it.krzeminski.githubactions.actions.actions.CacheV3
import it.krzeminski.githubactions.actions.actions.CheckoutV3
import it.krzeminski.githubactions.actions.actions.SetupJavaV3
import it.krzeminski.githubactions.actions.codecov.CodecovActionV3
import it.krzeminski.githubactions.domain.RunnerType
import it.krzeminski.githubactions.domain.Workflow
import it.krzeminski.githubactions.domain.triggers.Push
import it.krzeminski.githubactions.dsl.workflow
import it.krzeminski.githubactions.yaml.writeToFile
import java.nio.file.Paths

public val workflowBuild: Workflow = workflow(
      name = "Java CI",
      on = listOf(
        Push(),
        ),
      sourceFile = Paths.get(".github/kts/build.main.kts"),
    ) {
      job(
        id = "build",
        runsOn = RunnerType.UbuntuLatest,
      ) {
        uses(
          name = "Checkout",
          action = CheckoutV3(
            fetchDepth = CheckoutV3.FetchDepth.Infinite,
          ),
        )
        uses(
          name = "Cache",
          action = CacheV3(
            path = listOf(
              "~/.m2/repository",
            )
            ,
            key = "${'$'}{{ runner.os }}-maven-${'$'}{{ hashFiles('**/pom.xml') }}",
            restoreKeys = listOf(
              "${'$'}{{ runner.os }}-maven-",
            )
            ,
          ),
        )
        uses(
          name = "Set up JDK",
          action = SetupJavaV3(
            distribution = SetupJavaV3.Distribution.Corretto,
            javaVersion = "11",
          ),
        )
        run(
          name = "Build with Maven",
          command = "mvn -B install --file pom.xml"
        )
        uses(
          name = "CodecovAction",
          action = CodecovActionV3(
            token = "${'$'}{{ secrets.CODECOV_TOKEN }}",
            // _customVersion = "v1",
          ),
        )
      }

    }

workflowBuild.writeToFile()
