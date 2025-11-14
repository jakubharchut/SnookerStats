# Specyfikacja i Postęp Prac: Ekran Szczegółów Meczu i Statystyk

## Wersja: 1.0 (stan na 2025-11-15)

---

## 1. Cel Główny

Celem tego modułu jest stworzenie szczegółowego ekranu, który pozwoli użytkownikom na dogłębną analizę zakończonych meczy. Ekran prezentuje zarówno zagregowane statystyki dla całego meczu i poszczególnych frejmów, jak i szczegółową historię uderzeń.

---

## 2. Zaimplementowane Funkcjonalności

### 2.1. Ogólna Struktura (`MatchDetailsScreen`)

- Ekran podzielony na dwie główne zakładki: **"Statystyki"** i **"Historia wbić"**.
- Możliwość filtrowania widoku statystyk dla całego meczu lub pojedynczych frejmów za pomocą `FilterChip`.

### 2.2. Zakładka "Statystyki" (`StatsCard`)

- **Graficzne paski porównawcze (`AnimatedComparisonBar`)** dla kluczowych statystyk: Punkty, Najwyższy brejk, Czas przy stole.
- **Statystyka fauli:** Zastąpiono prosty licznik fauli nowym komponentem **"Punkty po faulach"**. Pokazuje on punkty *zdobyte* dzięki faulom przeciwnika, wraz z liczbą tych fauli w nawiasie (np. `6 (1 faule)`). Wykorzystuje również graficzny pasek do porównania.
- **Szczegółowe statystyki procentowe:** Zarówno "Skuteczność wbić", jak i "Skuteczność odstawnych" pokazują teraz stosunek udanych zagrań do wszystkich prób (np. `(15/20)`).
- **Interaktywne "Średnie punktowanie"**: Wartość tej statystyki jest klikalna i otwiera szczegółowy dialog.

### 2.3. Dialog "Podejścia punktowe" (`BreaksInfoDialog`)

- Wyświetlany po kliknięciu na statystykę "Średnie punktowanie".
- Prezentuje listę wszystkich podejść punktowych gracza.
- **Szczegółowy opis brejka**: Każdy wpis zawiera: numer frejma (np. `F1:`), pogrubioną wartość punktową z dopiskiem `pkt:` (np. `**65** pkt:`) oraz wizualną reprezentację wbitych bil.
- **Grupowanie i sortowanie bil**: Wbite bile są grupowane według koloru i sortowane zgodnie z ich wartością w snookerze (od czerwonej do czarnej).
- **Licznik na bilach**: Każda ikona bili wyświetla liczbę wbić danego koloru w ramach jednego brejka. Rozmiar ikon i czcionek został dostosowany dla lepszej czytelności.
- **Układ w jednej linii**: Wszystkie bile dla danego brejka są wyświetlane w jednym, przewijanym horyzontalnie rzędzie (`LazyRow`), co zapobiega niechcianemu zawijaniu wierszy.

### 2.4. Zakładka "Historia wbić" (`HistoryRow`)

- **Poprawne przypisanie fauli**: Informacja o faulu (`+X F`) jest teraz wyświetlana po stronie gracza, który na nim zyskał, a nie który go popełnił.
- **Dedykowane ikony**: Wprowadzono nowe, czytelne ikony dla fauli (`FoulIcon` - czerwony kwadrat z literą "F") oraz dla odstawnych (`SafetyIcon` - szary kwadrat), które zastępują generyczne ikony bil.
