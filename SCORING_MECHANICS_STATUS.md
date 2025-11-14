# Status Implementacji Mechaniki Gry (`ScoringViewModel`) - stan na 2025-11-13

Ten dokument podsumowuje zaimplementowane funkcjonalności i logikę walidacji dla ekranu wprowadzania wyników.

---

## 1. Zaimplementowane Funkcjonalności

- **[GOTOWE] Podstawowa logika gry:**
  - Wbijanie bil (czerwone -> kolor).
  - Sekwencja czyszczenia stołu (żółta -> czarna).
  - Obsługa "free ball" (z dedykowanego dialogu).
  - Zmiana aktywnego gracza.

- **[GOTOWE] Zegar frejma:**
  - Uruchamia się przy pierwszej akcji w podejściu.
  - Działa nieprzerwanie do końca frejma.

- **[GOTOWE] Faul:**
  - Dedykowany dialog do zgłaszania faulu z możliwością wyboru wartości i zadeklarowania wbitych czerwonych.

- **[GOTOWE] Wolna Bila (Free Ball):**
  - Dedykowany przycisk "Wolna bila", który otwiera osobne okno dialogowe.
  - Umożliwia wybór wbitej bili kolorowej oraz **ręczną modyfikację jej wartości punktowej**.
  - **Inteligentna wartość domyślna:** 1 punkt, gdy są czerwone na stole; wartość nominalna, gdy ich nie ma.
  - Akcja ta dodaje tylko punkty do wyniku i brejka, nie wpływając na stan gry (liczbę czerwonych, następną bilę, itd.).

- **[GOTOWE] Zakończenie Tury:**
  - Przyciski "Odstawna" i "Pudło" poprawnie kończą podejście i przekazują turę.

- **[GOTOWE] Inteligentne Zakończenie Frejma:**
  - **Automatyczne:** Frejm kończy się po wbiciu ostatniej czarnej bili (chyba, że jest remis).
  - **Ręczne (przycisk "Zakończ frejma"):** Przycisk jest zawsze aktywny.
    - **Logika Walidacji:** Po kliknięciu, system sprawdza, czy frejm można zakończyć normalnie (wynik nie jest 0:0, a przewaga punktowa jest wystarczająca).
    - **Scenariusz Prawidłowy:** Jeśli warunki są spełnione, pojawia się standardowy dialog podsumowujący frejm.
    - **Scenariusz Nieprawidłowy:** Jeśli warunków nie spełniono, pojawia się dialog z pytaniem, czy **przerwać i anulować** bieżącego frejma.
  - **Dogrywka na czarnej (Re-spotted Black):** System poprawnie obsługuje sytuację remisu po wbiciu ostatniej czarnej bili, inicjując dogrywkę.

- **[GOTOWE] Powtórzenie Frejma:**
  - Przycisk "Powtórz frejma" jest zawsze aktywny i zabezpieczony dialogiem potwierdzającym.

- **[GOTOWE] Zakończenie Meczu:**
  - Przycisk "Zakończ mecz" jest zawsze aktywny i otwiera dialog potwierdzający.

- **[GOTOWE] Cofnięcie Ruchu (`onUndoClicked`):**
  - Funkcjonalność zaimplementowana i działa w oparciu o modyfikację listy `shots` w Firestore.

- **[GOTOWE] Nawigacja i "Powrót do meczu":** Mechanizmy te są w pełni zaimplementowane.

## 2. Rzeczy do zrobienia (Następne kroki)

1.  **Implementacja Powiadomień o Meczu:** Stworzenie Cloud Function, która będzie informować przeciwnika o rozpoczęciu meczu.
2.  **Tryb Obserwatora:** Stworzenie mechanizmu, który pozwoli drugiemu graczowi dołączyć do trwającego meczu w trybie "tylko do odczytu".
3.  **Rozbudowa Modułu Treningowego:** Obecnie zakładka jest wyłączona. W przyszłości należy stworzyć dedykowany ekran z listą ćwiczeń (np. "Line-up").
