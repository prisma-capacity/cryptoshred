#!/usr/bin/env kotlin

@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.4.0")


@file:Repository("https://repo.maven.apache.org/maven2/")
@file:Repository("https://bindings.krzeminski.it")

@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:cache:v4")
@file:DependsOn("actions:setup-java:v4")
@file:DependsOn("codecov:codecov-action:v5")


import io.github.typesafegithub.workflows.actions.actions.Cache
import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.codecov.CodecovAction
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.ConsistencyCheckJobConfig


workflow(
  name = "Maven all in one",
  on = listOf(
    PullRequest(),
    Push(
      branches = listOf("main"),
    ),
  ),
  sourceFile = __FILE__,
  consistencyCheckJobConfig = ConsistencyCheckJobConfig.Disabled
) {
  job(
    id = "build",
    runsOn = RunnerType.UbuntuLatest,
  ) {
    uses(
      name = "Checkout",
      action = Checkout(fetchDepth = Checkout.FetchDepth.Infinite)
    )
    uses(
      name = "Cache - Maven Repository",
      action = Cache(
        path = listOf(
          "~/.m2/repository",
        ),
        key = "${'$'}{{ runner.os }}-maven-${'$'}{{ hashFiles('**/pom.xml') }}",
        restoreKeys = listOf(
          "${'$'}{{ runner.os }}-maven-",
        ),
      ),
    )
    uses(
      name = "JDK 17",
      action = SetupJava(
        distribution = SetupJava.Distribution.Corretto,
        javaVersion = "17",
      ),
    )

    run(
      name = "Build with Maven",
      command = "./mvnw -B clean install",
    )
    uses(
      name = "Codecov upload",
      action = CodecovAction(
        token = "${'$'}{{ secrets.CODECOV_TOKEN }}"
      ),
    )
  }
}


