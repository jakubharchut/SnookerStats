# Dokumentacja Logiki: Mechanika Wolnej Bili (Free Ball)

## Wersja: 2.0 (stan na 2025-11-13)

---

## 1. Cel Dokumentu

Ten dokument opisuje ostateczną, elastyczną implementację mechaniki "wolnej bili" w `ScoringViewModel`. Zastępuje ona poprzednie, sztywne systemy, dając użytkownikowi pełną kontrolę nad punktacją w tej wyjątkowej sytuacji.

---

## 2. Architektura Rozwiązania

Kluczowym założeniem nowego systemu jest dedykowany przycisk i dialog, które pozwalają na ręczne zadeklarowanie wbitej bili oraz (w razie potrzeby) modyfikację jej wartości punktowej. Logika nie jest już powiązana z oknem faulu.

### 2.1. Przepływ Danych i Interakcja

1.  **Aktywacja:** Użytkownik w dowolnym momencie może kliknąć przycisk **"Wolna bila"** na ekranie `ScoringScreen`.

2.  **Wyświetlenie Dialogu (`FreeBallDialog`):**
    *   Pojawia się okno dialogowe, które pozwala wybrać jedną z sześciu bil kolorowych.
    *   Dialog zawiera również pole numeryczne do ustawienia wartości punktowej przyznanej za wbitą bilę.

3.  **Inteligentna Wartość Domyślna:**
    *   Po wybraniu bili, pole punktowe jest automatycznie uzupełniane wartością domyślną:
        *   **1 punkt**, jeśli na stole wciąż są czerwone bile.
        *   **Nominalna wartość bili**, jeśli na stole nie ma już czerwonych bil.
    *   Użytkownik może ręcznie zmienić tę wartość w zakresie od 1 do 7.

4.  **Zatwierdzenie Akcji (`onFreeBallConfirmed`):
**    *   Po kliknięciu "Zatwierdź", `ViewModel` otrzymuje dwa parametry: wybraną bilę (`SnookerBall`) i jej ostateczną wartość punktową (`Int`).
    *   Tworzony jest nowy `Shot` z typem `ShotType.FREE_BALL_POTTED`.
    *   **Ważne:** Ten strzał dodaje tylko punkty do wyniku i brejka. **Nie wpływa on na stan gry na stole** – nie zmienia liczby czerwonych, nie aktywuje `canPotColor` ani nie przesuwa sekwencji kolorów `nextColorBallOn`.

---

## 3. Główne Zalety Obecnego Rozwiązania

*   **Pełna Kontrola Użytkownika:** Gracz może precyzyjnie odwzorować sytuację ze stołu, nawet jeśli jest ona nietypowa.
*   **Prostota i Niezawodność:** Logika jest prosta, jednoznaczna i nie próbuje "zgadywać" intencji gracza.
*   **Spójność Danych:** Dedykowany typ `ShotType.FREE_BALL_POTTED` gwarantuje, że rekonstrukcja stanu gry (`reconstructScoringState`) zawsze poprawnie odtworzy wynik, dodając punkty bez zmiany logiki frejma.
