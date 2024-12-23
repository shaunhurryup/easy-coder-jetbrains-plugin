plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "com.easycoder.intellij"
version = "0.0.12"

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
    // 不要改！让插件永远支持最新版本
    version.set("LATEST-EAP-SNAPSHOT")
    // 开发可以用老版本
//    version.set("2022.2.5")
    type.set("IC")
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set("221")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("EASY_CODER_JETBRAINS_PUBLISH_TOKEN"))
    }
}
