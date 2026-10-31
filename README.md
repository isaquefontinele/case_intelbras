# Case Intelbras by Isaque Fontinele

Desafio técnico proposto pela Intelbras: app Android (Kotlin + Jetpack Compose) para gerenciar dispositivos de vídeo (câmeras) e centrais de alarme, consumindo a API do desafio.

## Funcionalidades

- **Lista de dispositivos** com busca por nome, MAC Address ou número de série e filtros (Todos, Favoritos, Câmeras, Alarmes).
- **Adicionar, editar e remover** câmeras e alarmes, com validação dos campos antes de enviar e erros da API exibidos no campo correspondente (ex.: MAC ou número de série já cadastrado).
- **Informações do dispositivo**, com ação de copiar o MAC Address ou o número de série.
- **Favoritos locais**, que persistem entre reinícios do app e sincronizações.
- **Offline**: os últimos dados sincronizados continuam visíveis sem conexão.
- **Feedback** por snackbar e estados de carregando, lista vazia e erro; *pull to refresh* na lista.

## Requisitos

- Android Studio (2026.1 ou superior) com Android SDK 36
- JDK 17 ou superior
- minSdk 29 · targetSdk 36 · compileSdk 36

## Configuração do token da API

O token **não é versionado**. Ele é lido em tempo de build de `local.properties` (ignorado pelo git) ou da variável de ambiente `API_TOKEN`. O CI não precisa do token: ele compila e roda os testes, que não acessam a API real.

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

Com um emulador ou aparelho conectado:

```bash
./gradlew installDebug
```

Verificações (as mesmas do CI):

```bash
./gradlew ktlintCheck detekt lintDebug testDebugUnitTest :core:model:test assembleDebug
```

Os testes de UI (Compose) e de banco (Room) rodam na JVM com Robolectric, sem precisar de emulador.

## Arquitetura

MVVM com Coroutines/Flow, *offline-first*, com o Room como **fonte única de verdade**:

```
 Tela (Compose) ──ações──▶ ViewModel ──▶ Repository ──▶ API (Retrofit)
      ▲                        │              │
      └──── StateFlow ◀────────┘              ▼
                         observa ◀──────── Room
```

- **As telas só leem do banco.** O repositório expõe `Flow`s do Room; a API é usada para sincronizar (`refresh`) e para enviar criações, edições e remoções. Depois de cada operação bem-sucedida, o resultado é gravado no Room e a tela se atualiza sozinha.
- **Sincronização**: ao abrir a lista (e no *pull to refresh*), câmeras e alarmes são buscados em paralelo e substituem o conteúdo local numa única transação. Se a rede falhar, os dados locais continuam na tela e o erro aparece como snackbar.
- **Erros tipados**: falhas de rede e HTTP viram `DataError` (`NoConnection`, `Timeout`, `Unauthorized`, `NotFound`, `Invalid`, `Unexpected`) no `:core:data`, e cada feature traduz isso em mensagens para o usuário.
- **Telas sem estado**: cada tela recebe um `UiState` imutável e um objeto de ações, o que permite testá-la e pré-visualizá-la sem ViewModel. Eventos únicos (fechar o formulário, mostrar uma mensagem) passam por `Channel`.
- **Navegação** com rotas tipadas (Navigation Compose 2.9). O resultado do formulário ("Dispositivo adicionado/atualizado") volta para a lista pelo `SavedStateHandle`.
- **Injeção de dependência** com Hilt.

### Módulos

| Módulo | Responsabilidade |
|---|---|
| `:app` | Activity, navegação entre features, `Application` (Hilt) |
| `:core:model` | Modelos de domínio e validação (Kotlin puro) |
| `:core:network` | Retrofit/OkHttp, DTOs, interceptor de autenticação, mapeamento de erros HTTP |
| `:core:database` | Room: tabelas de dispositivos e favoritos, DAOs |
| `:core:data` | Repositório *offline-first* e sincronização |
| `:core:designsystem` | Tema (cores, tipografia Open Sans, claro/escuro) e componentes reutilizáveis |
| `:core:testing` | Dublês de teste compartilhados (`FakeDeviceRepository`, `MainDispatcherRule`) |
| `:feature:dashboard` | Lista, busca, filtros e menu de ações |
| `:feature:deviceform` | Adicionar / editar dispositivo |
| `:feature:deviceinfo` | Informações do dispositivo |
| `build-logic` | Convention plugins do Gradle (configuração comum dos módulos) |

As features não dependem umas das outras; só o `:app` as conhece e liga a navegação.

### Stack

Kotlin 2.4 · Jetpack Compose (BOM 2026.06) + Material 3 · Navigation Compose · Hilt · Room · Retrofit 3 + OkHttp 5 · kotlinx.serialization · Coroutines/Flow · JUnit 4 + Robolectric + MockWebServer · ktlint · detekt · GitHub Actions.

## Decisões

- **Favoritos em tabela separada.** A API não tem o conceito de favorito, então ele mora numa tabela `favorites` própria, ligada a `devices` por chave estrangeira com `ON DELETE CASCADE`. A sincronização usa *upsert* (e não `REPLACE`, que apaga e reinsere a linha e levaria o favorito junto pelo cascade). Dispositivos que sumiram do servidor são removidos, e o favorito deles também.
- **Uma tabela para os dois tipos.** Câmeras e alarmes ficam em `devices`, com uma coluna de tipo e os campos específicos opcionais. Os ids da API são UUIDs, então não há colisão entre os tipos, e a lista ordenada e a busca saem de uma única consulta.
- **Validação local alinhada à API.** A documentação não descreve todas as regras; testando a API, o servidor recusa com 400:
  - número de série que não tenha exatamente **13 caracteres**;
  - nome, usuário e senha com mais de **200 caracteres**;
  - senha de alarme que não seja numérica de 4 a 6 dígitos.

  O app valida isso antes de enviar, para mostrar o motivo no campo. Para a senha do alarme, segue a documentação (4 **ou** 6 dígitos), que é mais restrita que a API.
- **MAC Address normalizado.** O usuário pode digitar com ou sem separadores (`a0:b1:...` ou `A0B1...`); o app envia 12 caracteres hexadecimais em maiúsculas e exibe formatado com `:`.
- **Conflitos (409) no campo.** MAC ou número de série já cadastrado aparece como erro no próprio campo, e não como mensagem genérica.
- **Dispositivo removido por outra pessoa.** Editar um dispositivo que não existe mais no servidor (404) remove a cópia local e avisa na lista. Remover um que já não existe é tratado como sucesso.
- **Token inválido.** A documentação fala em 403, mas a API responde **401**; o app trata os dois como "não autorizado".
- **"Ativo" substituído por "Favorito".** O Figma mostra um status "Ativo" na tela de informações, mas a API não fornece esse dado. Para não exibir uma informação inventada, o cabeçalho mostra "★ Favorito" quando for o caso.
- **Senha nunca exibida** na tela de informações.
- **HTTP sem TLS.** A API só é servida em HTTP; o tráfego em texto puro é liberado apenas para esse host (`network_security_config.xml`).
- **API lenta.** As respostas levam alguns segundos (~5 s nos testes), por isso os timeouts do OkHttp são folgados e a lista mostra os dados locais enquanto sincroniza.
- **compileSdk 36.** Algumas bibliotecas em versões mais novas (ex.: OkHttp 5.5+) exigem compileSdk 37; foram mantidas na última versão compatível com o SDK pedido.
- **Ícones provisórios.** Os ícones vêm do Material Icons e estão centralizados em `AppIcons`, para facilitar a troca pelos do Figma.

## Testes

- **Regras de negócio**: validação e normalização (`:core:model`), repositório *offline-first* com fonte remota falsa e Room em memória (`:core:data`), mapeamento de erros.
- **Banco**: DAOs e mapeadores com Room em memória (Robolectric).
- **Rede**: chamadas, autenticação e mapeamento de erros HTTP com MockWebServer.
- **ViewModels**: busca, filtros, estados de carregamento/erro, formulário e informações.
- **UI**: telas e componentes com Compose UI Test (Robolectric).

## Qualidade e CI

- **ktlint** e **detekt** (configuração em `config/detekt/detekt.yml`) e **Android Lint**.
- **GitHub Actions** (`.github/workflows/ci.yml`): lint, testes e build a cada push e pull request.

## Pendências

- **Fila de operações offline**: hoje criar, editar e remover exigem conexão (sem rede, o app avisa e não salva). A ideia é gravar a operação no Room como pendente e enviá-la com WorkManager quando a rede voltar.
- **Tema escuro**: as cores escuras existem no design system, mas as telas ainda não foram revisadas nesse modo.
- **Acessibilidade**: descrições para leitor de tela e áreas de toque já estão nos componentes; falta uma revisão completa com TalkBack.
- **Ícones definitivos** do Figma.
- **Testes instrumentados** em aparelho (hoje todos os testes de UI e banco rodam com Robolectric).
