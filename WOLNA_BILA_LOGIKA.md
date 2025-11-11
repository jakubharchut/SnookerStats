# Dokumentacja Logiki: Mechanika Wolnej Bili (Free Ball)

## Wersja: 1.0 (stan na 2024-05-18)

---

## 1. Cel Dokumentu

Ten dokument opisuje ostateczną, uproszczoną i stabilną implementację mechaniki "wolnej bili" w `ScoringViewModel`. Została ona wprowadzona w celu zastąpienia poprzedniej, nadmiernie skomplikowanej i podatnej na błędy logiki.

---

## 2. Architektura Rozwiązania

Kluczowym założeniem nowego systemu jest **przeniesienie odpowiedzialności za deklarację bili na użytkownika** i oparcie logiki o jego jawną intencję, a nie próby jej "inteligentnego" odgadywania przez aplikację.

### 2.1. Przepływ Danych i Interakcja

1.  **Aktywacja Trybu:** Po faulu, gracz w oknie dialogowym zaznacza opcję "Wolna bila". W `ScoringViewModel` ustawiany jest stan `isFreeBall = true`.

2.  **Zmiana Interfejsu (UI):** W `ScoringScreen`, gdy `isFreeBall` jest `true`:
    *   Pojawia się wyraźny komunikat, np. "WOLNA BILA: Zadeklaruj wbitą bilę".
    *   Przycisk czerwonej bili jest **nieaktywny**.
    *   Wszystkie przyciski bil kolorowych są **aktywne**.

3.  **Akcja Użytkownika:** Gracz klika przycisk tej bili kolorowej, którą fizycznie zadeklarował i wbił.

4.  **Przekierowanie Logiki:** Główna funkcja `onBallClicked` posiada teraz prosty warunek na starcie:
    ```kotlin
    if (_uiState.value.isFreeBall) {
        onFreeBallPotted(ball)
        return
    }
    ```
    Dzięki temu cała logika wolnej bili jest zamknięta w jednej, dedykowanej funkcji.

### 2.2. Implementacja w `onFreeBallPotted(nominatedBall: SnookerBall)`

Ta funkcja zawiera dwa kluczowe, rozłączne scenariusze:

#### A. Scenariusz 1: Są jeszcze czerwone na stole (`redsRemaining > 0`)

*   **Zasada:** Wbita bila (niezależnie od jej koloru) jest traktowana jak czerwona.
*   **Logika:**
    *   Do wyniku i brejka dodawany jest **1 punkt**.
    *   Ustawiany jest stan `canPotColor = true`, aby umożliwić grę na właściwy kolor.
    *   `isFreeBall` jest resetowane do `false`.
    *   Zapisywany jest `Shot` z nowym, dedykowanym typem: `ShotType.FREE_BALL_POTTED_AS_RED`.

#### B. Scenariusz 2: Koniec gry, tylko kolory (`redsRemaining == 0`)

*   **Zasada:** Gracz otrzymuje punkty za bilę, która była aktualnie "na grze", ale musi ją wbić ponownie w następnym uderzeniu.
*   **Logika:**
    *   Pobierana jest wartość punktowa bili, która była w grze (np. 2 punkty za żółtą z `uiState.value.nextColorBallOn`).
    *   Ta wartość jest dodawana do wyniku i brejka.
    *   **Kluczowe:** Stan `nextColorBallOn` **NIE JEST ZMIENIANY**. Sekwencja kolorów nie postępuje.
    *   `isFreeBall` jest resetowane do `false`.
    *   Zapisywany jest `Shot` z nowym typem: `ShotType.FREE_BALL_POTTED_AS_COLOR`.

### 2.3. Odzwierciedlenie w `reconstructScoringState`

Aby zapewnić spójność (np. przy cofaniu ruchu), funkcja rekonstruująca stan gry została rozszerzona o obsługę nowych typów `Shot`:

*   Dla `FREE_BALL_POTTED_AS_RED`: dodaje 1 pkt i ustawia `canPotColor = true`.
*   Dla `FREE_BALL_POTTED_AS_COLOR`: dodaje punkty za `nextColorBallOn`, ale **nie przesuwa** `nextColorBallOn` do następnej bili.

---

## 3. Główne Zalety Obecnego Rozwiązania

*   **Stabilność i Niezawodność:** Logika jest prosta, jednoznaczna i odporna na błędy interpretacji.
*   **Łatwość Utrzymania:** Wszelkie przyszłe modyfikacje są ograniczone do jednej, dobrze odizolowanej funkcji.
*   **Spójność Danych:** Dedykowane typy `Shot` gwarantują, że rekonstrukcja stanu zawsze da ten sam wynik.
*   **Przejrzysty UX:** Użytkownik jest jasno informowany o wymaganym działaniu, co eliminuje pomyłki.
