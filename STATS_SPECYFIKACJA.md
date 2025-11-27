# Specyfikacja Modułu: Statystyki

## Wersja: 1.0 (stan na 2025-11-27)

---

## 1. Cel Główny

Celem modułu jest dostarczenie użytkownikowi kompleksowego narzędzia do analizy swoich postępów w grze. Ekran Statystyk prezentuje zagregowane dane z rozegranych meczy, z możliwością ich filtrowania według różnych kryteriów.

---

## 2. Struktura Ekranu (`StatsScreen`)

Ekran jest zbudowany w oparciu o `Scaffold` i podzielony na kilka kluczowych komponentów:

*   **`TopAppBar`**: Zawiera tytuł "Statystyki" oraz ikonę `FilterList`, która otwiera arkusz z filtrami.
*   **`TabRow`**: Umożliwia przełączanie się między dwiema głównymi sekcjami: "Mecze" i "Trening". Obecnie zaimplementowana jest tylko sekcja "Mecze".
*   **`MatchStatsContent`**: Główny obszar, w którym wyświetlane są statystyki meczowe.

### 2.1. Widok Statystyk Meczowych (`MatchStatsContent`)

Ten komponent składa się z dwóch głównych części:

1.  **`ActiveFiltersRow`**: Poziomy, przewijany wiersz, który wyświetla aktywne filtry. Każdy filtr jest reprezentowany przez `FilterChip` z ikoną "X", umożliwiającą jego usunięcie.
2.  **`LazyColumn` z Karta Statystyk**: Lista statystyk z możliwością rozwijania, umieszczona na jednej, dużej karcie. Każda pozycja statystyki (`ExpandableStatItem`) zawiera ikonę, etykietę, zagregowaną wartość i po rozwinięciu, pokazuje bardziej szczegółowe dane (`SubStatItem`).

---

## 3. System Filtrowania

System filtrowania jest kluczowym elementem tego modułu. Pozwala użytkownikowi na precyzyjne dopasowanie danych do swoich potrzeb.

### 3.1. Arkusz Filtrów (`StatsFilterSheet`)

Jest to `ModalBottomSheet`, który pojawia się po kliknięciu ikony `FilterList` w `TopAppBar`. Zawiera on następujące opcje:

*   **Rodzaj meczu**: `SegmentedButton` z opcjami "Wszystkie", "Sparingowe", "Rankingowe".
*   **Liczba czerwonych**: `SegmentedButton` z opcjami "All", "15", "10", "6", "3". Zmieniono "Wszystkie" na "All", aby uniknąć zawijania tekstu.
*   **Zakres dat**: Sekcja podzielona na dwie części:
    *   **Szybkie filtry**: Poziomy, przewijany wiersz z `FilterChip` dla predefiniowanych zakresów: "Wszystko", "Ost. 7 dni", "Ost. 30 dni", "Ost. 3 miesiące", "Ten rok".
    *   **Wybór ręczny**: Dwa przyciski ("Data początkowa", "Data końcowa"), które otwierają natywny `DatePickerDialog` do ręcznego wyboru dat.

### 3.2. Przepływ Danych w Filtrach

1.  **Stan filtrów**: Aktualny stan filtrów jest przechowywany w `StatsViewModel` w `StateFlow` o nazwie `filters` (typ `StatsFilters`).
2.  **Aplikowanie filtrów**: Po kliknięciu "Zastosuj" w `StatsFilterSheet`, `ViewModel` aktualizuje swoje `filters` i wywołuje funkcję `loadStats()`, która ponownie przelicza statystyki na podstawie nowych kryteriów.
3.  **Usuwanie filtrów**: Kliknięcie "X" na `FilterChip` w `ActiveFiltersRow` wywołuje funkcję `onFilterChipClosed(filterType)` w `ViewModelu`, która usuwa konkretny filtr i ponownie ładuje statystyki.
4.  **Logika filtrowania**: Cała logika filtrowania danych odbywa się po stronie klienta, w `ViewModelu`, wewnątrz funkcji `loadStats()`. Pobiera ona wszystkie mecze użytkownika z repozytorium, a następnie filtruje listę na podstawie aktywnych filtrów przed agregacją danych.

---

## 4. Wyświetlanie Statystyk

### 4.1. Wskaźnik Formy (`FormResultChip`)

Specjalny komponent do wizualizacji wyników ostatnich 5 meczy. Każdy wynik ("W", "P", "R") jest wyświetlany w małym, kolorowym prostokącie:

*   **"W" (Wygrana)**: Zielone tło z ciemnozielonym tekstem.
*   **"P" (Porażka)**: Czerwone tło (kolor `errorContainer`) z białym tekstem.
*   **"R" (Remis)**: Szare tło (kolor `secondaryContainer`) z ciemnym tekstem.

### 4.2. Rozwijane Statystyki (`ExpandableStatItem`)

Każda główna statystyka jest interaktywna. Kliknięcie na nią rozwija dodatkowy panel z bardziej szczegółowymi informacjami, co pozwala na zachowanie czystego i czytelnego interfejsu przy jednoczesnym dostępie do dużej ilości danych.
