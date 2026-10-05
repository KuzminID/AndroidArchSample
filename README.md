# AndroidArchSample

Шаблон Android-приложения на Jetpack Compose, организованного по принципам чистой
архитектуры с разбивкой на Gradle-модули по слоям и фичам. Репозиторий задуман как
**основа для форка** новых проектов: переносить отдельные модули не нужно, удобнее
форкнуть его целиком и строить поверх его структуры модулей, соглашений и настроенного стека.

Демо-функциональность: список персонажей из публичного
[Rick and Morty API](https://rickandmortyapi.com/) с экраном деталей, избранным и
сортировкой — работает офлайн из локального кэша.

## Стек технологий

| Назначение           | Библиотека                                                        |
|----------------------|-------------------------------------------------------------------|
| UI                   | Jetpack Compose (BOM 2026.09), Material 3                         |
| Навигация            | Navigation Compose 2.10, типизированные маршруты (`@Serializable`) |
| Состояние экранов    | `androidx.lifecycle.ViewModel`, `StateFlow` + `combine` + `stateIn` |
| DI                   | Hilt (граф проверяется при компиляции)                            |
| Локальная БД         | Room                                                              |
| Настройки            | DataStore (Preferences)                                           |
| Сеть                 | Retrofit + OkHttp                                                 |
| Сериализация         | kotlinx.serialization                                             |
| Изображения          | Coil 3                                                            |
| Асинхронность        | kotlinx.coroutines                                                |
| Логирование          | Timber за интерфейсом `Logger` из `core:common`                   |
| Тесты                | JUnit 4, kotlinx-coroutines-test, Turbine, Robolectric, ручные fake |
| Стиль и анализ       | ktlint 1.8, detekt 2.0 (alpha), Android Lint                      |
| Kotlin / AGP         | 2.4.10 / 9.1                                                      |

Версии всех зависимостей и инструментов зафиксированы в
[gradle/libs.versions.toml](gradle/libs.versions.toml).

### Риски обновления

Предрелизные инструменты и экспериментальные API, за которыми нужно следить при обновлении:

| Что | Где | Риск |
| --- | --- | --- |
| detekt 2.0.0-alpha.6 | сборка, `config/detekt/detekt.yml` | ключи конфигурации и правила могут измениться до 2.0 stable |
| `ExperimentalMaterial3Api`: `TopAppBar`, `PullToRefreshBox` | экраны `feature:characters:presentation` | изменение сигнатур в Material 3 |
| `ExperimentalCoroutinesApi`: `flatMapLatest` | `ObserveCharactersUseCase` | изменение API в kotlinx.coroutines |

## Использование как шаблона

Перед началом работы над новым проектом на основе этого репозитория замените
базовый пакет `ru.marwinka.androidarchsample` и имя `AndroidArchSample` одной
командой:

```bash
./scripts/rename-template.sh com.acme.myapp AcmeApp
```

`--dry-run` третьим аргументом покажет, что изменится, ничего не трогая. Скрипт
обрабатывает только файлы под git, поэтому требует чистое git-дерево (всё закоммичено
или проиндексировано). Он заменяет пакет и имя приложения, переносит каталоги
Kotlin-исходников под новый пакет и в конце прогоняет ktlint.

Скрипт **не** трогает имя файла Room-базы (`android_arch_sample.db` в `DatabaseModule`):
его меняют вручную и только в свежем форке без установленных пользователей.

Дальше: `core:*` и `design-system` оставьте как есть — это инфраструктура без
бизнес-логики примера, а `feature:characters` замените своими фичами (см. «Добавление
новой фичи»), используя её как образец связи слоёв.

## Структура модулей

```
app/                    Application, MainActivity + NavHost, Room AppDatabase (схема в app/schemas),
                        привязки DI: use case'ы, DispatcherProvider, Logger (Timber)
build-logic/            Gradle convention-плагины, проверка границ модулей, генератор фич

core/common/            Kotlin/JVM: AppResult/AppError, appResultOf, DispatcherProvider, Logger
core/network/           Retrofit/OkHttp/Json, повтор HTTP 429, маппинг сетевых ошибок в AppError
core/settings/          DataStore<Preferences> для настроек пользователя (ключи объявляют фичи)
core/testing/           Kotlin/JVM: MainDispatcherRule, TestDispatcherProvider, RecordingLogger
design-system/          Тема и общие Compose-компоненты (FullScreenLoading/Error)

feature/characters/     Фича "Персонажи" — 3 отдельных Gradle-модуля:
  ├── domain/           Kotlin/JVM: модели, интерфейсы репозиториев, ObserveCharactersUseCase;
  │                     testFixtures с общими FakeCharacterRepository/FakeCharacterSettingsRepository
  ├── data/             internal-репозитории, Retrofit-интерфейс и DTO (remote/),
  │                     Room Entity/DAO (local/), привязки Hilt (di/)
  └── presentation/     ViewModel, Route/Screen (+ @Preview), типизированные маршруты, строки
```

Каждый слой фичи — отдельный Gradle-модуль. `domain` и `core:common` — чистые
Kotlin/JVM-модули: Android API, Compose и Hilt в них физически недоступны. `presentation`
не видит `data`, Room и Retrofit: таких зависимостей нет в его classpath.

### Граф зависимостей между модулями

```
app ─┬─▶ feature:characters:presentation ─┬─▶ feature:characters:domain ─▶ core:common
     │                                     └─▶ design-system, core:common
     ├─▶ feature:characters:data ─┬─▶ feature:characters:domain
     │                             └─▶ core:network, core:settings, core:common
     ├─▶ feature:characters:domain   (DomainModule регистрирует use case'ы)
     └─▶ core:common, design-system
core:testing — только для тестов
```

`core:*` и `design-system` не зависят от фич. Фича хранит свои Room-сущности и DAO в
собственном `:data`, а общий `AppDatabase` объявлен в `app`: только точка сборки видит
сущности всех фич. Поэтому Entity и DAO публичны, а всё остальное в `:data` — `internal`.
Фичи видят друг друга только через модуль `:api` (в шаблоне пока не понадобился).

Правила проверяются автоматически. Плагин `androidarchsample.module-boundaries` из
`build-logic` на этапе конфигурации проверяет граф зависимостей между модулями
и роняет сборку при нарушении: `presentation → data`, `data → presentation`,
`domain → core:network`, обращение к чужой фиче не через `api`, `core:common → core:*`,
циклы, Android-плагин в `domain`. Сами правила покрыты тестами в `build-logic`.

## Архитектура внутри фичи (`feature/characters`)

- **`:domain`** — модели (`Character`, `SortOrder`), интерфейсы `CharacterRepository` и
  `CharacterSettingsRepository`, `ObserveCharactersUseCase` (комбинирует сортировку из
  настроек с кэшем). Use case заводится, только если в нём есть логика; простые операции
  ViewModel вызывает на репозитории напрямую. Разовые операции возвращают `AppResult`.
- **`:data`** — `CharacterRepositoryImpl`: offline-first, экраны читают Room, `refresh()`
  загружает все страницы API и одной транзакцией заменяет кэш. Избранное хранится в
  отдельной таблице `favorites` и при замене кэша не теряется; список с флагом избранного
  читается одним запросом. Сортировка — в DataStore (`CharacterSettingsRepositoryImpl`).
  Ошибки типизируются через `appResultOf` и `toNetworkError`, неожиданные пишутся в лог.
- **`:presentation`** — состояние экрана выводится из потоков: данные из Room,
  настройки и состояние обновления (`RefreshState`) собираются через `combine` + `stateIn`.
  Начальная загрузка и обновление различаются (`isInitialLoading` / `isRefreshing`,
  pull-to-refresh), одновременно идёт не больше одного обновления. Ошибка обновления на
  пустом кэше показывается на весь экран, поверх данных — в Snackbar. Ошибки — enum'ы,
  текст берётся из `strings.xml`.

## Добавление новой фичи

Скелет из 3 модулей генерируется таском `newFeature` (плагин
`androidarchsample.feature-generator` в `build-logic`):

```bash
./gradlew newFeature -PfeatureName=<FEATURE_NAME>
```

`featureName` — `movies` или `movie-list`/`movie_list`. Таск создаёт модули по образцу
`feature/characters` (пакеты `…feature.<name>.{domain,data,presentation}`): контракт
репозитория в `domain`, `internal`-реализацию с Hilt-привязкой в `data`, ViewModel, пару
Route/Screen и типизированный маршрут в `presentation`, — и дописывает `include(...)` в
[settings.gradle.kts](settings.gradle.kts). Сгенерированный код проходит ktlint и detekt.
Дальше — вручную, по шагам из сгенерированного `README.md` фичи.

## Данные

Стратегия изменений данных: изменения только локальные, синхронизации с сервером
нет. Избранное пишется в Room, сортировка — в DataStore, и экран получает
результат из хранилища, а не из ответа операции.

Room-база хранит кэш персонажей и избранное. Схема экспортируется в `app/schemas`, каждое
изменение схемы сопровождается миграцией и тестом в `AppDatabaseMigrationTest`;
деструктивная миграция отключена, потому что в базе есть данные пользователя.

API отдаёт персонажей страницами по 20 (42 страницы), `refresh()` проходит их по очереди.
API и аватарки стоят за общим лимитом Cloudflare (около 40 запросов, затем HTTP 429 с
`Retry-After: 10`). Поэтому `RateLimitInterceptor` в `core:network` пропускает запросы не
чаще раза в 300 мс, а после 429 приостанавливает все запросы клиента: на `Retry-After` или,
если там 0, на экспоненциальный backoff. Coil грузит картинки через тот же `OkHttpClient`
(см. `App`), чтобы они не расходовали лимит в обход ограничителя. Если лимит всё же
исчерпан, `refresh()` завершается ошибкой, а кэш остаётся прежним.

## Тесты

Юнит-тесты гоняются на JVM без устройства (`./gradlew test`); Room — в памяти через
Robolectric. Вместо моков — ручные fake: общие fake репозиториев лежат в `testFixtures`
модуля `:domain`, тестовые диспатчеры и логгер — в `core:testing`.

- `feature/characters/domain`: `ObserveCharactersUseCaseTest`
- `feature/characters/data`: DAO (замена кэша, сохранность избранного), репозитории
  (успех, ошибка сети, неожиданная ошибка, отмена), мапперы, настройки
- `feature/characters/presentation`: ViewModel'и — начальная загрузка и гонка с пустым кэшем,
  обновление, повтор, ошибки, избранное, сортировка
- `core/network`: маппинг ошибок, повтор HTTP 429
- `app`: миграции Room
- `build-logic`: правила границ модулей

```bash
./gradlew test
./gradlew -p build-logic test
```

## Сборка

Требования: JDK 17, Android SDK с `compileSdk 37`, Gradle 9.6 (через wrapper).

```bash
./gradlew assembleDebug    # debug APK
./gradlew lint detekt      # Android Lint и detekt
./gradlew ktlintCheck      # проверка стиля (ktlintFormat — автоисправление)
```

CI ([.github/workflows/ci.yml](.github/workflows/ci.yml)) на каждый push и pull request
запускает тесты `build-logic`, `ktlintCheck`, `detekt`, `lint`, юнит-тесты и сборку debug и
release. Чтобы слияние в `main` при падении CI блокировалось, включите в настройках GitHub
правило защиты ветки с обязательной проверкой `build`.

### Convention-плагины (`build-logic/`)

Included build (`pluginManagement { includeBuild("build-logic") }` в корневом
`settings.gradle.kts`) с precompiled-плагинами, которые модули подключают по id:

- **`androidarchsample.android.library`** / **`.android.application`** —
  `compileSdk`/`minSdk`/`compileOptions`; `namespace` и настройки приложения
  (`applicationId`, `versionName`) остаются в самом модуле.
- **`androidarchsample.jvm.library`** — Kotlin/JVM-модуль без Android (`domain`, `api`,
  `core:common`, `core:testing`).
- **`androidarchsample.android.compose`** — плагин компилятора Compose и зависимости
  BOM/UI/tooling. `buildFeatures { compose = true }` остаётся в `android {}` модуля:
  `compose` объявлен в `LibraryBuildFeatures`/`ApplicationBuildFeatures`, а не в
  `CommonExtension`, который может настроить общий плагин.
- **`androidarchsample.hilt`** — KSP, Gradle-плагин Hilt и пара
  `hilt-android`/`hilt-android-compiler`.
- **`androidarchsample.module-boundaries`** (корневой проект) — проверка границ модулей.
- **`androidarchsample.feature-generator`** (корневой проект) — таск `newFeature`.

Из-за встроенной в AGP 9 поддержки Kotlin Android-плагины **не** должны применять
`org.jetbrains.kotlin.android`: AGP подключает Kotlin сам, а явное применение ломает сборку.
