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
- **Rozpoczęcie Gry:** Przycisk "Rozpocznij Mecz" przenosi do `ScoringScreen` z przekazaniem wybranej liczby czerwonych jako argument nawigacji.

---

## 3. Mechanika Ekranu Wprowadzania Wyniku (`ScoringScreen`) - WIZJA

### 3.1. Kluczowe Założenia
- **Rejestracja Każdego Ruchu:** Aplikacja musi zapisywać każde uderzenie (`Shot`) jako osobny obiekt, zawierający typ (wbicie, faul, miss), wartość punktową oraz `timestamp`.
- **Synchronizacja na Żywo:** Mecz musi być w pełni synchronizowany w czasie rzeczywistym między dwoma urządzeniami graczy. Każda akcja wykonana przez jednego gracza jest natychmiast widoczna u drugiego.
- **Zasady Snookera:** Logika aplikacji musi uwzględniać kluczowe zasady gry, takie jak sekwencja wbijania bil (czerwona -> kolor), faule, `miss` oraz `free ball`.
- **Statystyki w Czasie Rzeczywistym:** Aplikacja musi na bieżąco obliczać i wyświetlać kluczowe dane, takie jak: aktualny break, punkty pozostałe na stole, liczba czerwonych na stole.
- **Wizualizacja Brejka:** Oprócz numerycznej wartości brejka, aplikacja graficznie przedstawia bile, które tworzą aktualny brejk. Jeśli w brejku znajduje się wiele bil tego samego koloru, reprezentacja graficzna odzwierciedla to za pomocą cyfry wewnątrz ikony bili.
- **Zegar Czasu Rozbicia (Break Timer):** Na ekranie wyświetlany jest zegar, który rozpoczyna odliczanie czasu od momentu wykonania pierwszego uderzenia (rozbicia) w danym podejściu.
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
    - **Implementacja wizualizacji brejka.**
    - **Dodanie komponentu zegara czasu rozbicia.**
- **Szacowany czas:** ok. 16 godzin.

### Dzień 4-5: Ożywienie Logiki w `ScoringViewModel`
- **Cel:** Zaimplementowanie wszystkich zasad snookera i logiki biznesowej gry.
- **Zadania:**
    - Połączenie `ScoringViewModel` z `MatchRepository` i nasłuchiwanie na `matchStream`.
    - Implementacja logiki dodawania uderzeń (`addShot`).
    - Zaimplementowanie walidacji ruchów (które bile są teraz aktywne).
    - Implementacja pełnej logiki fauli, w tym `miss` i `free ball`.
    - **Implementacja logiki dla zegara czasu rozbicia.**
    - **Implementacja logiki aktualizacji wizualizacji brejka.**
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

---

## 5. Implementacja `ScoringScreen` - Szczegóły Logiki (stan na 2025-11-10)

Architektura opiera się na `ScoringViewModel` oraz `ScoringState`, który przechowuje cały stan frejma. Poniżej opisano kluczowe zaimplementowane mechanizmy.

### 5.1. Logika Wbijania Bil (`onBallClicked`)
Funkcja ta jest sercem systemu i obsługuje wszystkie fazy gry:

#### A. Normalna Gra (czerwone na stole)
- **Czerwona bila jest zawsze dostępna:** Gracz może wbić czerwoną w każdej chwili. Pozwala to na obsługę scenariusza, w którym w jednym uderzeniu wpada więcej niż jedna czerwona – użytkownik po prostu klika przycisk "Czerwona" odpowiednią liczbę razy.
- **Aktywacja kolorów:** Po kliknięciu na czerwoną bilę, stan `canPotColor` jest ustawiany na `true`, co aktywuje przyciski bil kolorowych.
- **Dezaktywacja kolorów:** Po wbiciu bili kolorowej, `canPotColor` jest resetowany do `false`, zmuszając gracza do ponownego zagrania na czerwoną.

#### B. Wolna Bila (`isFreeBall`)
- **Aktywacja:** Tryb "Wolnej bili" jest aktywowany w oknie faulu.
- **Logika:** W tym trybie:
    - Przycisk czerwonej bili jest nieaktywny.
    - Wszystkie bile kolorowe są aktywne.
    - **Gdy są czerwone na stole:** Wbicie dowolnego koloru daje **1 punkt** (jak za czerwoną). Stan `canPotColor` jest ustawiany na `true`, aby umożliwić zagranie na normalny kolor.
    - **Gdy nie ma czerwonych na stole:** Wbicie dowolnego koloru daje punkty równe wartości bili, która była "na grze" w sekwencji.

#### C. Koniec Frejma (gra na kolory)
- **Aktywacja:** Tryb ten włącza się automatycznie, gdy `redsRemaining` spada do 0.
- **Krok 1: Dowolny kolor po ostatniej czerwonej:**
    - Bezpośrednio po wbiciu ostatniej czerwonej, stan `nextColorBallOn` jest ustawiany na `null`, a `canPotColor` na `true`. W UI powoduje to aktywację **wszystkich** bil kolorowych, pozwalając graczowi na wybór.
- **Krok 2: Sekwencja od żółtej do czarnej:**
    - Po wbiciu tej pierwszej, dowolnej bili kolorowej, `ViewModel` przechodzi w tryb sekwencyjny.
    - Stan `nextColorBallOn` jest ustawiany na `SnookerBall.Yellow`.
    - Od tego momentu tylko jeden, właściwy przycisk bili kolorowej jest aktywny, prowadząc gracza przez sekwencję: Żółta -> Zielona -> Brązowa -> Niebieska -> Różowa -> Czarna.
- **Koniec partii:** Po wbiciu czarnej bili, stan `isFrameOver` jest ustawiany na `true`, co blokuje wszystkie przyciski bil.

### 5.2. Logika Faulu (`onFoulConfirmed`)
- **Okno Dialogowe:** Po kliknięciu "Faul" pojawia się `AlertDialog` z trzema sekcjami:
    1.  **Wartość faulu:** Wybór od 4 do 7 punktów.
    2.  **Wolna bila:** Checkbox do aktywacji trybu `isFreeBall` dla przeciwnika.
    3.  **Czerwone wbite w faulu:** Licznik (+/-) pozwalający określić, ile czerwonych bil wpadło do kieszeni podczas faulu.
- **Działanie:** Po zatwierdzeniu, `ViewModel`:
    - Dodaje wybraną liczbę punktów do wyniku **przeciwnika**.
    - Zmniejsza `redsRemaining` o podaną liczbę.
    - Przekazuje turę przeciwnikowi, aktywując dla niego tryb `isFreeBall`, jeśli został zaznaczony.

### 5.3. Zakończenie Tury (`endTurn`)
- Funkcje `onMissClicked` ("Pudło") oraz `onSafetyClicked` ("Odstawna") wywołują wspólną, prywatną funkcję `endTurn()`.
- **Działanie:** Funkcja ta:
    - Zmienia aktywnego gracza (`activePlayerId`).
    - Zeruje `currentBreak` i `breakHistory`.
    - Resetuje wszystkie stany (takie jak `canPotColor` i `isFreeBall`) do wartości początkowych dla nowej tury.
    - Jeśli gra jest już w fazie na kolory, poprawnie ustawia `nextColorBallOn` dla nadchodzącego gracza.
