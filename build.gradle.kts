plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "com.easycoder.intellij"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("cn.hutool:hutool-all:5.8.22")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.41")
    implementation("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30") // 添加注解处理器
}

intellij {
    version.set("2022.2.5")
    type.set("IC")
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("233.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(project.findProperty("intellijPublishToken").toString())
    }
}
