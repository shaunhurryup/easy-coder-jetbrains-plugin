plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "com.easycoder.intellij"
version = "0.0.5"

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
    // 设置为 LATEST-EAP-SNAPSHOT,这样可以兼容最新的 EAP 版本
    version.set("2022.2.5")
    // 将插件类型设置为 IC (IntelliJ Community),这样可以兼容社区版和旗舰版
    type.set("IC") 
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        // 设置最低兼容版本为 2022.1
        sinceBuild.set("222")
        // 移除 untilBuild 的设置,这样可以兼容未来的所有版本
        // untilBuild.set("301.*")
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
