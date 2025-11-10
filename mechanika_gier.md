# Specyfikacja Modułu: Mechanika Gry

## Wersja: 1.0 (stan na 2025-11-10)

---

## 1. Cel Główny

Celem tego modułu jest zdefiniowanie wszystkich mechanik związanych z rozgrywką - od sposobu inicjowania meczu, przez jego konfigurację, aż po interfejs wprowadzania wyników i logikę zapisu.

---

## 2. Inicjowanie Meczu (Przepływ Użytkownika - UX)

Aby zapewnić elastyczność i intuicyjność, zdefiniowano dwie główne ścieżki rozpoczynania nowego meczu.

### 2.1. Ścieżka 1: Inicjowanie z Profilu Gracza

Jest to najszybsza, kontekstowa metoda rozpoczęcia gry z konkretnym przeciwnikiem.

1.  Użytkownik wchodzi na profil innego gracza (znajomego lub wyszukanego).
2.  Klika przycisk **"Rozpocznij mecz"**.
3.  Następuje bezpośrednie przejście do ekranu **`MatchSetupScreen`**, z automatycznie wybranym przeciwnikiem.
4.  Na tym ekranie użytkownik konfiguruje szczegóły meczu:
    *   **Rodzaj:** Rankingowy / Sparingowy.
    *   **Format:** Liczba czerwonych bil (np. 15, 10, 6).
5.  Kliknięcie **"Rozpocznij"** przenosi do ekranu wprowadzania wyniku (`ScoringScreen`).

### 2.2. Ścieżka 2: Inicjowanie z Zakładki "Graj"

Jest to główna, centralna ścieżka, która obsługuje wszystkie możliwe scenariusze gry.

1.  Użytkownik wchodzi w zakładkę **"Graj"** na dolnym pasku nawigacyjnym.
2.  Na ekranie `PlayScreen` widzi kilka opcji do wyboru:
    *   **"Wybierz sparingpartnera"** (do gry z innym zarejestrowanym użytkownikiem).
    *   **"Zagraj z gościem"** (gdy przeciwnik nie ma konta w aplikacji).
    *   **"Trening solo"** (do samotnej gry).
3.  W zależności od wyboru, przepływ wygląda następująco:
    *   **A) Wybór sparingpartnera:**
        *   Użytkownik jest przenoszony do ekranu **`OpponentSelectionScreen`**, który zawiera listę jego znajomych oraz wyszukiwarkę.
        *   Po wybraniu przeciwnika, przechodzi do ekranu `MatchSetupScreen` (jak w ścieżce 1).
    *   **B) Gra z gościem:**
        *   Użytkownik przechodzi do `MatchSetupScreen`, gdzie zamiast wyboru profilu, dostępne jest pole tekstowe do wpisania **imienia gościa**.
    *   **C) Trening solo:**
        *   Użytkownik przechodzi do `MatchSetupScreen` bez wybranego przeciwnika.

### 2.3. Podsumowanie i Spójność Architektury

Podejście to zapewnia:
- **Wiele punktów wejścia** do rozpoczęcia meczu, co zwiększa wygodę.
- **Współdzielony i reużywalny ekran `MatchSetupScreen`**, który jest centralnym punktem konfiguracji gry, niezależnie od ścieżki.
- **Obsługę wszystkich kluczowych scenariuszy**: gra ze znajomym, gra z osobą spoza aplikacji oraz trening solo.

---

## 3. Plan Implementacji (Etap 6)

Realizacja powyższego przepływu wymaga stworzenia i połączenia nawigacją następujących ekranów:

1.  **`OpponentSelectionScreen`**: Ekran z listą znajomych i wyszukiwarką, służący do wyboru przeciwnika.
2.  **`MatchSetupScreen`**: Ekran konfiguracji rodzaju i formatu meczu.
3.  **`ScoringScreen`**: Główny ekran do wprowadzania wyniku uderzenie po uderzeniu.
