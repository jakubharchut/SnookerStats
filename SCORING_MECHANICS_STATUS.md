# Status Implementacji Mechaniki Gry (`ScoringViewModel`) - stan na 2025-11-12

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
  - **Walidacja:** Akcja jest blokowana, jeśli wynik jest remisowy.
  - **Dialog Podsumowujący:** Po zakończeniu frejma pojawia się dialog z wynikiem, zwycięzcą i opcjami: "Następny frejm", "Zakończ mecz", "Wróć do frejma".

- **[GOTOWE] Powtórzenie Frejma:**
  - Przycisk "Powtórz frejma" jest dostępny po zakończeniu podejścia.
  - **Walidacja:** Akcja jest zabezpieczona dialogiem z prośbą o potwierdzenie.
  - Logika poprawnie resetuje bieżący frejm.

- **[GOTOWE] Zakończenie Meczu:**
  - Przycisk "Zakończ mecz" jest dostępny po zakończeniu podejścia.
  - **Ulepszona Walidacja:** Akcja jest blokowana tylko wtedy, gdy bieżący frejm jest w trakcie (wynik inny niż 0-0). Zezwala na zakończenie meczu, gdy frejm jest nierozpoczęty.
  - Logika poprawnie ustawia status meczu na `COMPLETED` i ignoruje nierozpoczęte frejmy przy zapisie.

- **[GOTOWE] Cofnięcie Ruchu (`onUndoClicked`):**
  - Funkcjonalność zaimplementowana i działa w oparciu o modyfikację listy `shots` w Firestore, zapewniając synchronizację stanu gry.

- **[GOTOWE] Nawigacja po zakończeniu meczu:** Po `onEndMatchConfirmed` aplikacja automatycznie przenosi użytkownika do ekranu historii meczy.

- **[GOTOWE] Implementacja "Powrót do meczu":** Mechanizm pozwalający kontynuować przerwany mecz po ponownym uruchomieniu aplikacji został w pełni zaimplementowany, wykorzystując lokalną bazę danych Room i automatyczne przekierowanie z ekranu "Graj".

## 2. Rzeczy do zrobienia (Następne kroki)

1.  **Implementacja Powiadomień o Meczu:** Stworzenie Cloud Function, która będzie informować przeciwnika o rozpoczęciu meczu.
2.  **Tryb Obserwatora:** Stworzenie mechanizmu, który pozwoli drugiemu graczowi dołączyć do trwającego meczu w trybie "tylko do odczytu".
