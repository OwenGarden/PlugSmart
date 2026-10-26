# Design – PlugSmart

## App Overview

- **Developer:** Owen Garden – 2309802
- **App Title:** PlugSmart
- **Purpose:** Shows the cheapest times today to use electricity (e.g. charging an EV or running the washing machine), using half-hourly prices from the Octopus Energy Agile API.

**Core functionalities**

- See today's half-hourly electricity prices, colour-coded cheap/medium/expensive – *Must 1–2, Should 1, 4*
- See the cheapest time to plug in, highlighted at the top of the screen – *Must 1–2*
- Pick an appliance and get the cheapest start time for how long it runs – *Should 3*
- Works offline using the last saved prices – *Could 6 (Room)*
- Downloads tomorrow's prices automatically each evening – *Could 7 (WorkManager)*
- "Remind me" notification before the cheapest time – *Could 8 (Notifications)*
- Keeps state on rotation and shows clear error/offline messages – *Must 3, Should 2, 5*

## UI/UX Design & The View Layer

**Wireframes** (to be replaced with screenshots)

```
Today screen                     Plan screen
+-------------------------+      +-------------------------+
| PlugSmart               |      | < Plan an appliance     |
| [Offline banner]        |      | (Washing) (Dishwasher)  |
| +---------------------+ |      | (EV charge) (Custom)    |
| | Cheapest: 02:00-05:00| |      | +---------------------+ |
| | 8.2p/kWh             | |      | | Start at 01:30       | |
| +---------------------+ |      | | 40% cheaper than avg | |
| 00:00  12.1p   [amber]  |      | +---------------------+ |
| 00:30   9.4p   [green]  |      | [ Remind me ]           |
| 01:00   8.7p   [green]  |      +-------------------------+
| ...                     |
| [ Plan an appliance ]   |
+-------------------------+
```

**Compose UI structure**

- `TodayScreen`: `Scaffold`, `TopAppBar`, a `Card` for the cheapest time, a `LazyColumn` of `PriceRow` items, and a `Button` to open the Plan screen.
- `PlanScreen`: `FilterChip`s for appliances, a result `Card`, and a "Remind me" `Button`.

**State hoisting**

- Stateless composables: `PriceRow`, `CheapestCard`, `ApplianceChips`, `ResultCard`, `OfflineBanner`. They receive data and callbacks (e.g. `onApplianceSelected`) as parameters.
- State is hoisted to the screen composables, which collect it from their ViewModel.

**Responsive design**

- Portrait: a single column (card above the list).
- Landscape: two panes, with the card on the left and the list on the right.
- The app uses Material 3 colours, typography and components throughout.

## MVVM Architecture & State Management

**ViewModels**

| ViewModel | Exposes (`StateFlow`) |
|---|---|
| `TodayViewModel` | `TodayUiState(slots, cheapestWindow, isLoading, isOffline, lastUpdated, error)` |
| `PlanViewModel` | `PlanUiState(appliance, durationMinutes, bestWindow, savingPercent, reminderSet)` |

ViewModels survive rotation, so their `StateFlow` state is kept across configuration changes.

**Unidirectional data flow (choosing an appliance)**

1. The user taps "EV charge" → `PlanScreen` calls `viewModel.onApplianceSelected(EV)`.
2. The ViewModel finds the cheapest run of consecutive slots lasting 4 hours.
3. The ViewModel updates `_uiState` with the best window.
4. `PlanScreen` collects `uiState` and recomposes to show the new start time.

**Data layer – Repository pattern**

- `PriceRepository` is the single source of truth.
- `getPrices()` returns a `Flow<List<PriceSlot>>` from Room.
- `refresh()` calls the API with Retrofit and saves the results to Room.
- The UI always reads from Room, so offline mode works automatically.

**Room entity**

```kotlin
@Entity(tableName = "price_slots")
data class PriceSlot(
    @PrimaryKey val validFrom: Long,   // epoch millis
    val validTo: Long,
    val pricePence: Double,            // inc VAT, p/kWh
    val region: String,                // e.g. "P" (North Scotland)
    val fetchedAt: Long
)
```

**API JSON (received)**

`GET https://api.octopus.energy/v1/products/{product}/electricity-tariffs/E-1R-{product}-{region}/standard-unit-rates/`

```json
{
  "count": 48,
  "results": [
    {
      "value_exc_vat": 7.81,
      "value_inc_vat": 8.2,
      "valid_from": "2026-10-27T02:00:00Z",
      "valid_to": "2026-10-27T02:30:00Z"
    }
  ]
}
```

## Navigation & Resilience

**Destinations (type-safe Compose Navigation)**

```kotlin
@Serializable object TodayRoute
@Serializable data class PlanRoute(val durationMinutes: Int = 120)
```

**Passing data**

- Only the appliance duration is passed in `PlanRoute`.
- Prices are not passed between screens. Both ViewModels read them from the repository.

**Error handling**

- Network failure: `refresh()` is wrapped in `try/catch`. The ViewModel sets `isOffline = true` and shows the saved prices with an offline banner.
- No saved data and offline: the app shows a message and a Retry button.
- Empty API response (e.g. tomorrow's prices aren't published yet): the app shows "Tomorrow's prices are published around 4pm".
- Database errors are caught in the repository, and the error is shown in the UI state.
