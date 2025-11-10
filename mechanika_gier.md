# Specyfikacja Modułu: Mechanika Gry

## Wersja: 1.1 (stan na 2025-11-10)

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

Jest to główna, centralna ścieżka, która obsługuje wszystkie możliwe scenariusze gry.

1.  Użytkownik wchodzi w zakładkę **"Graj"** na dolnym pasku nawigacyjnym.
2.  Widzi interfejs oparty na zakładkach: "Gracze", "Gość", "Trening", "Turniej".
3.  Wybór opcji przenosi go do `MatchSetupScreen` w odpowiednim trybie.

### 2.3. Podsumowanie i Spójność Architektury

Podejście to zapewnia:
- **Wiele punktów wejścia** do rozpoczęcia meczu, co zwiększa wygodę.
- **Współdzielony i reużywalny ekran `MatchSetupScreen`**, który jest centralnym punktem konfiguracji gry, niezależnie od ścieżki.
- **Obsługę wszystkich kluczowych scenariuszy**: gra ze znajomym, gra z osobą spoza aplikacji oraz trening solo.

---

## 3. Ulepszenia Interfejsu Inicjacji Meczu (Listopad 2025)

Wprowadzono szereg ulepszeń w interfejsie użytkownika modułu gry, aby był on bardziej spójny z resztą aplikacji i bardziej funkcjonalny.

### 3.1. Ekran "Graj" w Stylu Zakładek
- **Problem:** Początkowa wersja ekranu "Graj" z trzema przyciskami była niespójna z resztą aplikacji.
- **Rozwiązanie:** Ekran `PlayScreen` został gruntownie przebudowany. Główna nawigacja opiera się teraz na komponencie `TabRow` (zakładki), spójnym z ekranem "Społeczność".
- **Zakładki:**
    - **Gracze:** Do rozpoczynania meczu z zarejestrowanymi użytkownikami.
    - **Gość:** Do gry z przeciwnikiem bez konta.
    - **Trening:** Do gry solo.
    - **Turniej:** Zakładka-placeholder, zarezerwowana dla przyszłej funkcjonalności turniejów.

### 3.2. Grupowana Lista Przeciwników z Ulubionymi
- **Problem:** Prosta lista znajomych była nieefektywna przy większej liczbie kontaktów.
- **Rozwiązanie:** W zakładce "Gracze" zaimplementowano zaawansowaną, grupowaną i rozwijaną listę przeciwników.
    - **System Ulubionych:** Użytkownik może oznaczać graczy jako "ulubionych" za pomocą ikony gwiazdki, co przenosi ich do dedykowanej grupy na górze listy.
    - **Grupowanie:** Lista jest podzielona na rozwijane sekcje: "Ulubieni", "Klubowicze" i "Pozostali znajomi". Zapewnia to przejrzystość i przygotowuje aplikację pod pełną funkcjonalność klubów.
    - **Wyszukiwarka:** Poniżej listy znajduje się przycisk "Szukaj gracza", który przenosi do pełnej wyszukiwarki w module "Społeczność".

### 3.3. Ulepszenia Ekranu Konfiguracji Meczu
- **Dodano format "3 Czerwone":** Rozszerzono opcje formatu meczu o popularny wariant treningowy.
- **Przycisk Wstecz:** Dodano przycisk "Wstecz", aby umożliwić użytkownikowi łatwy powrót do ekranu wyboru przeciwnika.
- **Spójny Wygląd:** Zaktualizowano sposób wyświetlania informacji o graczu, aby był spójny z innymi częściami aplikacji (Imię Nazwisko + @username).

---

## 4. Plan Implementacji (Etap 6)

Realizacja powyższego przepływu wymaga stworzenia i połączenia nawigacją następujących ekranów:

1.  **`PlayScreen`**: Zaimplementowany z zakładkami i listą graczy.
2.  **`MatchSetupScreen`**: Zaimplementowany szkielet UI i ViewModel.
3.  **`ScoringScreen`**: Główny ekran do wprowadzania wyniku uderzenie po uderzeniu (do zrobienia).
