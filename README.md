# Case Intelbras by Isaque Fontinele

Desafio técnico proposto pela Intelbras: app Android (Kotlin + Jetpack Compose) para gerenciar dispositivos de vídeo e alarme.

## Requisitos

- Android Studio (2026.1 ou superior) com Android SDK 36
- JDK 17
- minSdk 29 · targetSdk 36 · compileSdk 36

## Configuração do token da API

O token **não é versionado**. Ele é lido em tempo de build de `local.properties` (git-ignored) ou da variável de ambiente `API_TOKEN` (usada no CI).

1. Copie o arquivo de exemplo:
   ```bash
   cp local.properties.example local.properties
   ```
2. Edite `local.properties` e preencha:
   ```properties
   sdk.dir=/caminho/para/Android/sdk
   api.token=SEU_TOKEN_AQUI
   ```
   Opcionalmente, `api.baseUrl` sobrescreve a URL da API (padrão `http://squadapps.ddns-intelbras.com.br:37025/`).

O valor vira `BuildConfig.API_TOKEN` no módulo `:core:network`.

## Rodando

```bash
./gradlew installDebug
```

Verificações (as mesmas do CI):

```bash
./gradlew ktlintCheck detekt lintDebug testDebugUnitTest assembleDebug
```

## Estrutura de módulos

| Módulo | Responsabilidade |
|---|---|
| `:app` | Activity, navegação, `Application` (Hilt) |
| `:core:model` | Modelos de domínio (Kotlin puro) |
| `:core:network` | Retrofit/OkHttp, DTOs, interceptor de autenticação |
| `:core:database` | Room (fonte única de verdade) |
| `:core:data` | Repositórios, sincronização, fila de operações |
| `:core:designsystem` | Tema (cores, tipografia Open Sans) e componentes reutilizáveis |
| `:feature:dashboard` | Lista, busca, filtros e menu de ações |
| `:feature:deviceform` | Adicionar / editar dispositivo |
| `:feature:deviceinfo` | Informações do dispositivo |
| `build-logic` | Convention plugins do Gradle |
