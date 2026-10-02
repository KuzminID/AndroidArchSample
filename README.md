# AndroidArchSample

Шаблон Android-приложения на Jetpack Compose, организованного по принципам чистой
архитектуры с разбивкой на Gradle-модули по слоям и фичам. Репозиторий задуман как
**основа для форка** новых проектов: переносить отдельные модули не нужно, удобнее
форкнуть его целиком и строить поверх его структуры модулей, соглашений и настроенного стека.

Демо-функциональность: список персонажей из публичного
[Rick and Morty API](https://rickandmortyapi.com/) с экраном деталей, избранным и
сортировкой — работает офлайн из локального кэша.

## Стек технологий

| Назначение          | Библиотека                              |
|----------------------|------------------------------------------|
| UI                   | Jetpack Compose (BOM 2026.09), Material 3 |
| Навигация            | Navigation Compose 2.10, типизированные маршруты (`@Serializable`) |
| Состояние экранов     | `androidx.lifecycle.ViewModel`, `StateFlow` + `stateIn` |
| DI                    | Hilt                                     |
| Локальная БД          | Room                                     |
| Настройки             | DataStore (Preferences) с миграцией из `SharedPreferences` |
| Сеть                  | Retrofit + OkHttp                        |
| Сериализация          | kotlinx.serialization                    |
| Изображения           | Coil 3                                   |
| Асинхронность         | kotlinx.coroutines                       |
| Kotlin / AGP          | 2.4.10 / 9.1                             |

Версии всех зависимостей зафиксированы в [gradle/libs.versions.toml](gradle/libs.versions.toml).

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

Что скрипт **не** трогает — внутренние строковые литералы, не совпадающие ни с пакетом,
ни с именем: имя старого файла `SharedPreferences` в `core:preferences`
(`android_arch_sample_prefs`) и имя файла Room-базы в `app` (`android_arch_sample.db`,
`DatabaseModule`). Первое используется только миграцией в DataStore — меняйте его лишь
в свежем форке без установленных пользователей.

Дальше: `core:*` оставьте как есть — это инфраструктура без бизнес-логики примера, а
`feature:characters` замените своими фичами (см. «Добавление новой фичи»), используя её
как образец связи слоёв.

## Структура модулей

```
app/                    Application, MainActivity + NavHost, DomainModule (use case'ы),
                        Room AppDatabase + DatabaseModule (схема в app/schemas)
build-logic/            Gradle convention-плагины (общие настройки Android-модулей)

core/common/            DispatcherProvider, AppResult/AppError (типизированные ошибки)
core/network/           Retrofit/OkHttp, CharacterApi + DTO, маппинг сетевых ошибок в AppError
core/preferences/       Обёртка над DataStore<Preferences> (избранное, порядок сортировки)
core/ui/                Тема и общие Compose-компоненты (FullScreenLoading/Error)
core/testing/           MainDispatcherRule и другие тестовые утилиты (чистый JVM)

feature/characters/     Фича "Персонажи" — 3 отдельных Gradle-модуля:
  ├── domain/           модели, интерфейс репозитория, use case'ы — без Android и DI-аннотаций;
  │                     testFixtures с общим FakeCharacterRepository
  ├── data/             репозиторий, Room Entity/DAO фичи (local/), привязка Hilt (di/)
  └── presentation/     ViewModel, Route/Screen (+ @Preview), типизированные маршруты, строки
```

Каждый слой фичи — это отдельный Gradle-модуль, а не просто пакет. Границы
Clean Architecture проверяются компилятором: `presentation` физически не может
импортировать Room или Retrofit (нет такой зависимости в classpath), а `domain` не видит
ни Android, ни Compose, ни Hilt.

### Граф зависимостей между модулями

```
app ─┬─▶ feature:characters:presentation ─┬─▶ feature:characters:domain ─▶ core:common
     │                                     └─▶ core:ui, core:common
     ├─▶ feature:characters:data ─┬─▶ feature:characters:domain
     │                             └─▶ core:network, core:preferences, core:common
     ├─▶ feature:characters:domain   (DomainModule регистрирует use case'ы)
     └─▶ core:*
core:testing — только для тестов
```

`core:*` — "нижние" слои без зависимостей от фич. Фича хранит свои Room-сущности и DAO
в собственном `:data`, а общий `AppDatabase` объявлен в `app`: только точка сборки может
видеть сущности всех фич. Там же `DomainModule` регистрирует use case'ы, чтобы `:domain`
оставался без DI-аннотаций. Фичи видят друг друга только через `:domain`.

Эти правила не только описаны, но и проверяются: корневой
[build.gradle.kts](build.gradle.kts) на этапе конфигурации проверяет зависимости между
проектами и роняет сборку при нарушении (например, `presentation → data`,
`core → feature` или обращение к чужому `data`/`presentation`).

## Архитектура внутри фичи (`feature/characters`)

Фича — 3 Gradle-модуля по слоям чистой архитектуры:

- **`:domain`** — модели (`Character`, `SortOrder`), интерфейс репозитория
  (`CharacterRepository`), use case'ы (`GetCharactersUseCase`, `GetCharacterDetailUseCase`,
  `RefreshCharactersUseCase`, `ToggleFavoriteUseCase`, `GetSortOrderUseCase`,
  `SetSortOrderUseCase`). Чистый Kotlin без DI-аннотаций; разовые операции возвращают
  `AppResult` с типизированным `AppError`.
- **`:data`** — `CharacterRepositoryImpl`: offline-first репозиторий, который объединяет
  Retrofit (сеть), Room (кэш) и DataStore (избранное, сортировка) в единый источник правды.
  Здесь же `CharacterEntity`/`CharacterDao` и Hilt-модуль `DataModule` с `@Binds`.
- **`:presentation`** — `ViewModel` со `StateFlow<UiState>` (собирается через
  `combine` + `stateIn`), stateful `…Route` (берёт ViewModel через `hiltViewModel()`) и
  stateless `…Screen` с `@Preview`, типизированные маршруты `CharactersListDestination` /
  `CharacterDetailDestination(characterId)`. Тип ошибки (`CharactersListError`) превращается
  в текст на экране из `strings.xml`. Зависит только от `:domain` — не видит `:data`.

## Добавление новой фичи

Скелет из 3 модулей (`domain/data/presentation`) генерируется таском `newFeature`
в корневом [build.gradle.kts](build.gradle.kts):

```bash
./gradlew newFeature -PfeatureName=<FEATURE_NAME>
```

`featureName` — `movies` или `movie-list`/`movie_list`. Таск создаёт модули по образцу
`feature/characters`: контракт `<Name>Repository` в `domain`, `internal`-реализацию с
Hilt-привязкой в `data`, `<Name>ViewModel` (`stateIn`), пару `<Name>Route`/`<Name>Screen`
и `<Name>NavGraph` в `presentation`, — и дописывает `include(...)` в
[settings.gradle.kts](settings.gradle.kts). Дальше — вручную (шаги перечислены в
сгенерированном `README.md` фичи): заменить заглушку репозитория, подключить `:data` и
`:presentation` в `app/build.gradle.kts` и зарегистрировать навграф в `MainActivity`.

## Данные

`CharacterRepositoryImpl.refresh()` загружает из Rick and Morty API все страницы
персонажей и одной транзакцией заменяет ими кэш в Room, поэтому удалённые на сервере
записи не задерживаются. Экраны читают только из Room, так что список доступен офлайн, а
ошибка сети показывается поверх уже загруженных данных (Snackbar) или на весь экран, если
кэш пуст. Room-база — чистый кэш: при смене схемы она пересоздаётся
(`fallbackToDestructiveMigration`) и заново наполняется из сети. Избранное и порядок
сортировки хранятся в DataStore; значения из прежнего файла `SharedPreferences`
переносятся при первом чтении.

## Тесты

Юнит-тесты гоняются на JVM без устройства (`./gradlew test`); фейки вместо моков там,
где это возможно, — общий `FakeCharacterRepository` лежит в `testFixtures` модуля `:domain`:

- `feature/characters/domain`: тесты всех use case'ов (`GetCharactersUseCaseTest` и др.)
- `feature/characters/data`: `CharacterRepositoryImplTest`
- `feature/characters/presentation`: `CharactersListViewModelTest`, `CharacterDetailViewModelTest`
- `core/network`: `NetworkErrorsTest` (маппинг исключений в `AppError`)
- `core/preferences`: `UserPreferencesTest` (DataStore во временном файле)

```bash
./gradlew test
```

## Сборка

Требования: JDK 17, Android SDK с `compileSdk 37`, Gradle 9.6 (через wrapper).

```bash
./gradlew assembleDebug    # debug APK
./gradlew lint             # Android Lint
./gradlew ktlintCheck      # проверка стиля (ktlintFormat — автоисправление)
```

CI ([.github/workflows/ci.yml](.github/workflows/ci.yml)) на каждый push и pull request
запускает `ktlintCheck`, `lint`, юнит-тесты и `assembleDebug`.

### Convention-плагины (`build-logic/`)

Included build (`pluginManagement { includeBuild("build-logic") }` в корневом
`settings.gradle.kts`) с precompiled-плагинами, которые модули подключают по id:

- **`androidarchsample.android.library`** / **`.android.application`** —
  `compileSdk`/`minSdk`/`compileOptions`; `namespace` и настройки приложения
  (`applicationId`, `versionName`) остаются в самом модуле.
- **`androidarchsample.android.compose`** — плагин компилятора Compose и зависимости
  BOM/UI/tooling. `buildFeatures { compose = true }` остаётся в `android {}` модуля:
  `compose` объявлен в `LibraryBuildFeatures`/`ApplicationBuildFeatures`, а не в
  `CommonExtension`, который может настроить общий плагин.
- **`androidarchsample.hilt`** — KSP, Gradle-плагин Hilt и пара
  `hilt-android`/`hilt-android-compiler`.

Из-за встроенной в AGP 9 поддержки Kotlin эти плагины **не** должны применять
`org.jetbrains.kotlin.android`: AGP подключает Kotlin сам, а явное применение ломает сборку.
