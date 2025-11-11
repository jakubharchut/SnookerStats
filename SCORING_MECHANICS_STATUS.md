# Status Implementacji Mechaniki Gry (`ScoringViewModel`) - stan na 2025-11-11

Ten dokument podsumowuje zaimplementowane funkcjonalności i logikę walidacji dla ekranu wprowadzania wyników.

---

## 1. Zaimplementowane Funkcjonalności

- **[GOTOWE] Podstawowa logika gry:**
  - Wbijanie bil (czerwone -> kolor).
  - Sekwencja czyszczenia stołu (żółta -> czarna).
  - Obsługa "free ball".
  - Zmiana aktywnego gracza.

- **[GOTOWE] Zegar frejma:**
  - Uruchamia się przy pierwszej akcji w podejściu (wbicie, faul, odstawa, pudło).
  - Działa nieprzerwanie do końca frejma (resetowany przez `onNextFrameClicked` lub `onRepeatFrameConfirmed`).

- **[GOTOWE] Faul:**
  - Dedykowany dialog do zgłaszania faulu.
  - Możliwość wyboru wartości (4-7), zadeklarowania "free ball" i określenia liczby czerwonych wbitych w faulu.

- **[GOTOWE] Zakończenie Tury:**
  - Przyciski "Odstawna" i "Pudło" poprawnie kończą podejście i przekazują turę.

- **[GOTOWE] Zakończenie Frejma (Automatyczne i Ręczne):**
  - **Automatyczne:** Frejm kończy się po wbiciu ostatniej czarnej bili.
  - **Ręczne:** Przycisk "Zakończ frejma" jest dostępny po zakończeniu podejścia.
  - **Walidacja:** Akcja jest blokowana, jeśli:
    - Wynik jest remisowy.
    - Różnica punktów jest mniejsza niż liczba punktów pozostałych na stole.
  - **Dialog Podsumowujący:** Po zakończeniu frejma pojawia się dialog z wynikiem, zwycięzcą i opcjami: "Następny frejm", "Zakończ mecz", "Wróć do frejma".

- **[GOTOWE] Powtórzenie Frejma:**
  - Przycisk "Powtórz frejma" jest dostępny po zakończeniu podejścia.
  - **Walidacja:** Akcja jest zabezpieczona dialogiem z prośbą o potwierdzenie.
  - Logika poprawnie resetuje bieżący frejm.

- **[GOTOWE] Zakończenie Meczu:**
  - Przycisk "Zakończ mecz" jest dostępny po zakończeniu podejścia.
  - **Walidacja:** Akcja jest blokowana, jeśli bieżący frejm nie jest jeszcze rozstrzygnięty.
  - **Walidacja:** Akcja jest zabezpieczona dialogiem z prośbą o potwierdzenie.
  - Logika poprawnie ustawia status meczu na `COMPLETED`.

- **[UWAGA - DO POPRAWY] Cofnięcie Ruchu (`onUndoClicked`):**
  - Funkcjonalność zaimplementowana, ale działa **tylko lokalnie** (`stateHistory`).
  - **Krytyczny problem:** W trybie online spowoduje to desynchronizację stanu gry między dwoma urządzeniami. Wymaga przebudowy w oparciu o modyfikację listy `shots` w Firestore.

## 2. Rzeczy do zrobienia (Następne kroki)

1.  **Przebudowa `onUndoClicked`:** Implementacja cofania ruchu w oparciu o zapis w Firestore (najwyższy priorytet).
2.  **Nawigacja po zakończeniu meczu:** Po `onEndMatchConfirmed` aplikacja powinna automatycznie przenieść użytkownika do ekranu historii meczy.
3.  **Implementacja "Powrót do meczu":** Stworzenie mechanizmu, który pozwoli kontynuować przerwany mecz po ponownym uruchomieniu aplikacji.
