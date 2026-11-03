# Case Intelbras by Isaque Fontinele

Desafio técnico proposto pela Intelbras: app Android (Kotlin + Jetpack Compose) para gerenciar dispositivos de vídeo (câmeras) e centrais de alarme, consumindo a API do desafio.

## Funcionalidades

- **Lista de dispositivos** com busca por nome, MAC Address ou número de série e filtros (Todos, Favoritos, Câmeras, Alarmes).
- **Adicionar, editar e remover** câmeras e alarmes, com validação dos campos antes de enviar e erros da API exibidos no campo correspondente (ex.: MAC ou número de série já cadastrado).
- **Informações do dispositivo**, com ação de copiar o MAC Address ou o número de série.
- **Favoritos locais**, que persistem entre reinícios do app e sincronizações.
- **Offline**: os últimos dados sincronizados continuam visíveis sem conexão, e criar, editar e remover funcionam sem rede. As mudanças são enviadas automaticamente quando a conexão volta, mesmo com o app fechado. A lista e a tela de Informações mostram o que ainda aguarda envio.
- **Feedback** por snackbar e estados de carregando, lista vazia e erro; *pull to refresh* na lista.
- **Tema claro e escuro**, seguindo a configuração do sistema.
- **Português e inglês**, seguindo o idioma do sistema.

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
./gradlew ktlintCheck detekt lintDebug testDebugUnitTest :core:model:test :core:insights:jvmTest assembleDebug
```

Os testes de UI (Compose) e de banco (Room) rodam na JVM com Robolectric, sem precisar de emulador.

Para instalar a versão de release, com R8 (assinada com a chave de debug, só para avaliação):

```bash
./gradlew installRelease
```

## Arquitetura

MVVM com Coroutines/Flow, *offline-first*, com o Room como **fonte única de verdade**:

```
 Tela (Compose) ──ações──▶ ViewModel ──▶ Repository ──▶ API (Retrofit)
      ▲                        │              │
      └──── StateFlow ◀────────┘              ▼
                         observa ◀──────── Room
```

- **As telas só leem do banco.** O repositório expõe `Flow`s do Room; a API é usada para sincronizar (`refresh`) e para enviar criações, edições e remoções. Depois de cada operação bem-sucedida, o resultado é gravado no Room e a tela se atualiza sozinha.
- **Sincronização**: ao abrir a lista (e no *pull to refresh*), as mudanças pendentes são enviadas e câmeras e alarmes são buscados em paralelo, substituindo o conteúdo local numa única transação. Se a rede falhar, os dados locais continuam na tela e o erro aparece como snackbar.
- **Fila offline**: quando o servidor não responde, a escrita é salva no Room como pendente e um worker do WorkManager, com restrição de rede, a envia quando a conexão volta.
- **Erros tipados**: falhas de rede e HTTP viram `DataError` (`NoConnection`, `Timeout`, `Unauthorized`, `NotFound`, `Invalid`, `Unexpected`) no `:core:data`, e cada feature traduz isso em mensagens para o usuário.
- **Telas sem estado**: cada tela recebe um `UiState` imutável e um objeto de ações, o que permite testá-la e pré-visualizá-la sem ViewModel. Eventos únicos (fechar o formulário, mostrar uma mensagem) passam por `Channel`.
- **Navegação** com rotas tipadas (Navigation Compose 2.9). O resultado do formulário ("Dispositivo adicionado/atualizado") volta para a lista pelo `SavedStateHandle`.
- **Injeção de dependência** com Hilt.

### Módulos

| Módulo | Responsabilidade |
|---|---|
| `:app` | Activity, navegação entre features, `Application` (Hilt) |
| `:core:model` | Modelos de domínio e validação (Kotlin puro) |
| `:core:insights` | Resumo dos dispositivos da lista, em Kotlin Multiplatform (alvos JVM e iOS) |
| `:core:network` | Retrofit/OkHttp, DTOs, interceptor de autenticação, mapeamento de erros HTTP |
| `:core:database` | Room: tabelas, DAOs e migrações |
| `:core:data` | Repositório *offline-first*, sincronização e fila de mudanças pendentes (WorkManager) |
| `:core:designsystem` | Tema (cores, tipografia Open Sans, claro/escuro) e componentes reutilizáveis |
| `:core:testing` | Dublês de teste compartilhados (`FakeDeviceRepository`, `MainDispatcherRule`) |
| `:feature:dashboard` | Lista, busca, filtros e menu de ações |
| `:feature:deviceform` | Adicionar / editar dispositivo |
| `:feature:deviceinfo` | Informações do dispositivo |
| `build-logic` | Convention plugins do Gradle (configuração comum dos módulos) |

As features não dependem umas das outras; só o `:app` as conhece e liga a navegação.

### Stack

Kotlin 2.4 (com um módulo Multiplatform) · Jetpack Compose + Material 3 · Navigation Compose · Hilt · Room · Retrofit 3 + OkHttp 5 · kotlinx.serialization · Coroutines/Flow · JUnit 4 + Robolectric + MockWebServer · ktlint · detekt · GitHub Actions.

## Decisões

### Dados e fila offline

- **Favoritos em tabela separada.** A API não tem favoritos, então eles ficam em `favorites`, que a sincronização não toca. Em `devices`, ela usa *upsert*: `REPLACE` apaga e reinsere a linha, e o `ON DELETE CASCADE` levaria o favorito junto.
- **Uma tabela para os dois tipos.** Os ids da API são UUIDs, então não colidem, e a lista e a busca saem de uma só consulta. O custo são colunas opcionais.
- **Fila como estado do dispositivo.** Cada linha tem um `syncState`, em vez de existir uma tabela de operações. Assim, as mudanças se juntam sozinhas: criar e editar vira uma criação, criar e remover não envia nada, e editar e remover envia só a remoção.
- **API primeiro.** Só falta de conexão e tempo limite viram pendência, para que erros como MAC já cadastrado continuem aparecendo no campo.
- **Pendências preservadas.** O download não sobrescreve linhas com mudança não enviada. Dispositivos criados offline usam um id local, e a troca pelo id do servidor fica registrada, para o favorito e as telas abertas acompanharem.
- **Uma operação por vez.** Um `Mutex` serializa escritas e sincronizações, incluindo a rede, para uma sincronização não sobrescrever uma edição. O custo é que salvar pode esperar uma sincronização em andamento.
- **Recusas não são repetidas.** Um 409 ou outro 4xx é desfeito e a lista avisa, mesmo que o envio tenha ocorrido com o app fechado. Falhas temporárias são repetidas com espera exponencial.

### API

- **Regras descobertas testando.** O servidor exige série com exatamente 13 caracteres e textos com até 200, e responde 401 para token inválido, embora a documentação fale em 403. O app valida antes de enviar. Na senha do alarme, segue a documentação (4 ou 6 dígitos), mais restrita que a API (4 a 6).
- **MAC normalizado.** Aceita com ou sem separadores, envia 12 caracteres hexadecimais em maiúsculas e exibe com `:`.
- **Dispositivo removido por outra pessoa.** Editar (404) remove a cópia local e avisa na lista. Remover é tratado como sucesso.
- **API lenta e sem TLS.** Com respostas de cerca de 5 s, os timeouts são folgados e a lista mostra os dados locais enquanto sincroniza. O HTTP em texto puro é liberado só para esse host.
- **compileSdk 36.** Algumas bibliotecas ficaram na última versão compatível, porque as mais novas (ex.: OkHttp 5.5+) exigem o 37.

### Interface e extras

- **"Favorito" no lugar de "Ativo".** O Figma mostra um status "Ativo", que a API não fornece, e preferi não inventar o dado. A senha nunca é exibida em Informações.
- **Ícones do Figma.** São Vector Drawables centralizados em `AppIcons`. O Figma não tem o olho aberto de "mostrar senha", então usei o desenho do Material.
- **Idiomas.** O inglês fica em `values/` e o português em `values-pt/`, então um aparelho em outro idioma vê o app em inglês.
- **Acessibilidade.** Revisei a semântica, as áreas de toque e o layout com fonte em 200%. Um erro de validação também gera snackbar, para ser anunciado na hora.
- **Kotlin Multiplatform em um módulo novo.** O `:core:insights` calcula o resumo abaixo do contador da lista, que não está no protótipo. Preferi algo pequeno a migrar um módulo que já funcionava. O Android consome o alvo JVM; os alvos iOS compilam e rodam os testes, mas nenhum app iOS os usa.

## Testes

- **Regras de negócio**: validação e normalização (`:core:model`), repositório *offline-first* com fonte remota falsa e Room em memória (`:core:data`), mapeamento de erros.
- **Banco**: DAOs, mapeadores e migração de schema com Room (Robolectric).
- **Fila offline**: combinações de mudanças pendentes, recusas do servidor e o worker de sincronização (`:core:data`).
- **Rede**: chamadas, autenticação e mapeamento de erros HTTP com MockWebServer.
- **ViewModels**: busca, filtros, estados de carregamento/erro, formulário e informações.
- **UI**: telas e componentes com Compose UI Test (Robolectric).

## Qualidade e CI

- **ktlint** e **detekt** (configuração em `config/detekt/detekt.yml`) e **Android Lint**.
- **GitHub Actions** (`.github/workflows/ci.yml`): lint, testes e build a cada push e pull request.

## Pendências

- **Fila offline**: se uma criação estourar o tempo limite mas tiver sido feita no servidor, o reenvio recebe 409. O download traz o dispositivo, mas um favorito marcado nesse meio-tempo se perde e o usuário vê o aviso de recusa sem necessidade.
- **Acessibilidade**: falta uma rodada completa com TalkBack feita por quem usa leitor de tela no dia a dia.
- **Testes instrumentados** em aparelho (hoje todos os testes de UI e banco rodam com Robolectric).

## O que eu melhoraria com mais tempo

- **Segurança**: o token fica no `BuildConfig`, e por isso pode ser extraído do APK; o certo seria autenticar o usuário e receber um token de sessão. As senhas dos dispositivos ficam em texto puro no Room, onde poderiam ser criptografadas com SQLCipher ou Keystore. E o tráfego precisaria de HTTPS, que depende da API.
- **Conflitos na sincronização**: hoje a mudança local pendente vence o que está no servidor, sem comparar. Com uma versão ou data de alteração na API, daria para detectar a edição concorrente e perguntar ao usuário.
- **Performance**: medir recomposições e uso de memória, e adicionar Baseline Profile e Macrobenchmark para o início do app.
- **Testes de ponta a ponta**: fluxos completos em aparelho contra um servidor de teste, cobrindo também o build de release, que hoje só testei manualmente.
- **CI e distribuição**: gerar o build de release com R8 no CI, assinar com uma chave própria e publicar por um canal de teste.
- **Listas grandes**: paginação com Paging 3, se a API passar a suportar.
- **UI**: animações nas transições entre telas e estados, e seletor de idioma nas configurações.
