# FoxNet VPN

**Android VPN client based on [FPTN](https://github.com/batchar2/fptn).**

FoxNet VPN is an independently developed version of the FPTN Android client, featuring a redesigned interface and ongoing improvements to usability and application settings.

---

## Интерфейс

<!-- SCREENSHOT 1: Главный экран в отключённом и подключённом состояниях -->

| VPN отключён | VPN подключён |
|:---:|:---:|
| ![Главный экран — VPN отключён](docs/screenshots/connection-disconnected.png) | ![Главный экран — VPN подключён](docs/screenshots/connection-connected.png) |

<!-- SCREENSHOT 2: Выбор сервера и настройки туннелирования -->

| Выбор сервера | Настройки туннелирования |
|:---:|:---:|
| ![Выбор сервера](docs/screenshots/server-selection.png) | ![Настройки туннелирования](docs/screenshots/tunneling-settings.png) |

<!-- SCREENSHOT 3: Дополнительные настройки и управление приложениями -->

| Дополнительные настройки | VPN для отдельных приложений |
|:---:|:---:|
| ![Дополнительные настройки](docs/screenshots/advanced-settings.png) | ![Настройки приложений](docs/screenshots/per-app-vpn.png) |

---

## Возможности

- **Управление подключением** — подключение и отключение VPN с главного экрана.
- **Выбор сервера** — просмотр и выбор доступных серверов.
- **Настройки туннелирования** — управление параметрами VPN-туннеля.
- **VPN для отдельных приложений** — настройка маршрутизации трафика выбранных приложений.
- **Дополнительные настройки** — доступ к экспериментальным и вспомогательным параметрам.
- **Информация о проекте** — сведения о приложении, исходном проекте и лицензии.

## Загрузка

**FoxNet VPN 1.0.0** — первый стабильный выпуск проекта.

Актуальные версии доступны на странице **[GitHub Releases](https://github.com/NFX-boop/FoxNet-VPN/releases)**.

### Доступные сборки

| Пакет | Архитектура / назначение |
|---|---|
| `arm64-v8a.apk` | FOSS — 64-битные ARM-устройства |
| `armeabi-v7a.apk` | FOSS — 32-битные ARM-устройства |
| `.aab` | GMS — распространение через Google Play |

APK-сборки не включают компоненты Google и Firebase. AAB собирается с использованием GMS-варианта приложения.

При ручной установке APK выбира
