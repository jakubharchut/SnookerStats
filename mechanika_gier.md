# Specyfikacja Modułu: Mechanika Gry

## Wersja: 1.2 (stan na 2025-11-10)

---

## 1. Cel Główny

Celem tego modułu jest zdefiniowanie wszystkich mechanik związanych z rozgrywką - od sposobu inicjowania meczu, przez jego konfigurację, aż po interfejs wprowadzania wyników i logikę zapisu.

---

## 2. Inicjowanie Meczu (Przepływ Użytkownika - UX)

Aby zapewnić elastyczność i intuicyjność, zdefiniowano dwie główne ścieżki rozpoczynania nowego meczu.

### 2.1. Ścieżka 1: Inicjowanie z Profilu Gracza

Jest to najszybsza, kontekstowa metoda rozpoczęcia gry z konkretnym przeciwnikiem.

1.  Użytkownik wchodzi na profil innego gracza.
2.  Klika przycisk **"Zagraj"**.
3.  Następuje bezpośrednie przejście do ekranu **`MatchSetupScreen`**, z automatycznie wybranym przeciwnikiem.

### 2.2. Ścieżka 2: Inicjowanie z Zakładki "Graj"

Jest to główna, centralna ścieżka, która obsługuje wszystkie możliwe scenariusze gry. Ekran "Graj" został zaimplementowany w formie zakładek, aby zapewnić spójność z innymi częściami aplikacji.

- **Zakładka "Gracze":**
    1.  Wyświetla pogrupowaną listę potencjalnych przeciwników: "Ulubieni", "Klubowicze", "Pozostali znajomi".
    2.  Umożliwia dodawanie/usuwanie graczy do/z "Ulubionych" za pomocą ikony gwiazdki.
    3.  Zawiera przycisk "Szukaj gracza", który przenosi do pełnej wyszukiwarki w module "Społeczność".
    4.  Po wybraniu gracza z listy, użytkownik przechodzi do `MatchSetupScreen`.

- **Zakładka "Gość":**
    1.  Pozwala na rozpoczęcie gry z przeciwnikiem, który nie ma konta w aplikacji.
    2.  Przenosi do `MatchSetupScreen`, gdzie należy wpisać imię gościa.

- **Zakładka "Trening":**
    1.  Umożliwia rozpoczęcie gry solo.
    2.  Przenosi do `MatchSetupScreen` w trybie treningu.
    
- **Zakładka "Turniej":**
    1.  Placeholder dla przyszłej funkcjonalności turniejowej.

### 2.3. Ekran Konfiguracji Meczu (`MatchSetupScreen`)
Jest to centralny punkt konfiguracji gry, niezależnie od ścieżki, którą wybrał użytkownik.

- **Wyświetlanie Przeciwnika:** Pokazuje, z kim gramy (profil gracza, pole na imię gościa, lub informacja o treningu solo).
- **Przycisk Wstecz:** Umożliwia łatwy powrót do poprzedniego ekranu.
- **Konfiguracja:**
    - **Rodzaj Meczu:** Sparingowy / Rankingowy.
    - **Format Meczu:** Liczba czerwonych bil (15, 10, 6, 3).
- **Rozpoczęcie Gry:** Przycisk "Rozpocznij Mecz" przenosi do `ScoringScreen`.

---

## 3. Mechanika Ekranu Wprowadzania Wyniku (`ScoringScreen`) - WIZJA

### 3.1. Kluczowe Założenia
- **Rejestracja Każdego Ruchu:** Aplikacja musi zapisywać każde uderzenie (`Shot`) jako osobny obiekt, zawierający typ (wbicie, faul, miss), wartość punktową oraz `timestamp`.
- **Synchronizacja na Żywo:** Mecz musi być w pełni synchronizowany w czasie rzeczywistym między dwoma urządzeniami graczy. Każda akcja wykonana przez jednego gracza jest natychmiast widoczna u drugiego.
- **Zasady Snookera:** Logika aplikacji musi uwzględniać kluczowe zasady gry, takie jak sekwencja wbijania bil (czerwona -> kolor), faule, `miss` oraz `free ball`.
- **Statystyki w Czasie Rzeczywistym:** Aplikacja musi na bieżąco obliczać i wyświetlać kluczowe dane, takie jak: aktualny break, punkty pozostałe na stole, liczba czerwonych na stole, informacja o potrzebie snookera.
- **Powrót do Gry:** Użytkownik musi mieć możliwość powrotu do niedokończonego meczu po przypadkowym zamknięciu aplikacji.
- **Wizualizacja:** Zebrane dane muszą być wystarczająco szczegółowe, aby w przyszłości umożliwić stworzenie wizualizacji przebiegu frejma (np. w formie histogramu).

---

## 4. Harmonogram Wdrożenia (Etap 6)

### Dzień 1: Fundamenty i Szkielet Danych
- **Cel:** Przygotowanie całej architektury pod logikę gry.
- **Zadania:**
    - Rozszerzenie modeli danych (`Match`, `Frame`, `Shot`).
    - Stworzenie interfejsu `MatchRepository`.
    - Stworzenie podstawowej implementacji `MatchRepositoryImpl`.
    - Stworzenie szkieletu `ScoringViewModel`.
- **Szacowany czas:** ok. 7-8 godzin.

### Dzień 2-3: Implementacja `ScoringScreen` (UI)
- **Cel:** Zbudowanie kompletnego, ale jeszcze "głupiego" (bez pełnej logiki) interfejsu użytkownika.
- **Zadania:**
    - Stworzenie górnego panelu z wynikami i informacjami o graczach.
    - Implementacja panelu statystyk bieżących (break, punkty na stole itp.).
    - Dodanie wszystkich przycisków akcji (bile, faule, koniec podejścia).
    - Stworzenie UI dla okna dialogowego faulu.
- **Szacowany czas:** ok. 16 godzin.

### Dzień 4-5: Ożywienie Logiki w `ScoringViewModel`
- **Cel:** Zaimplementowanie wszystkich zasad snookera i logiki biznesowej gry.
- **Zadania:**
    - Połączenie `ScoringViewModel` z `MatchRepository` i nasłuchiwanie na `matchStream`.
    - Implementacja logiki dodawania uderzeń (`addShot`).
    - Zaimplementowanie walidacji ruchów (które bile są teraz aktywne).
    - Implementacja pełnej logiki fauli, w tym `miss` i `free ball`.
- **Szacowany czas:** ok. 16 godzin.

### Dzień 6: Synchronizacja, Powiadomienia i Finalizacja
- **Cel:** Sprawienie, by mecz działał w czasie rzeczywistym między dwoma graczami.
- **Zadania:**
    - Implementacja Cloud Function `sendMatchInvitationNotification`.
    - Pełna implementacja `startNewMatch` w repozytorium (tworzenie dokumentu w Firestore).
    - Implementacja mechanizmu "Wróć do gry".
    - Testy end-to-end i poprawki.
- **Szacowany czas:** ok. 8 godzin.

**Całkowity szacowany czas:** ok. 6-7 dni roboczych.
