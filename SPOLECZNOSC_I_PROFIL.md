# Specyfikacja Modułu: Społeczność i Profil

## Wersja: 1.0 (stan na 2024-07-25)

---

## 1. Cel Główny

Celem tego modułu jest stworzenie w pełni funkcjonalnego systemu społecznościowego, który umożliwi użytkownikom interakcję, budowanie sieci kontaktów (znajomych) oraz zarządzanie swoim publicznym profilem. Moduł ten jest kluczowy dla budowania zaangażowania i umożliwienia rywalizacji między graczami.

---

## 2. Model Danych (User)

Centralnym modelem danych dla tego modułu jest klasa `User`. Będzie ona przechowywać wszystkie informacje o profilu i relacjach społecznościowych użytkownika.

**Definicja `data class User` (ZREALIZOWANO):**
*   `uid: String`
*   `username: String`
*   `email: String`
*   `firstName: String?`
*   `lastName: String?`
*   `club: String?`
*   `profileImageUrl: String?`
*   `friends: List<String>`
*   `friendRequestsSent: List<String>`
*   `friendRequestsReceived: List<String>`
*   `isRealNameVisible: Boolean`

---

## 3. Plan Implementacji Funkcjonalności

### Krok 1: Onboarding - Uzupełnienie Profilu po Rejestracji [ZREALIZOWANO]

**Cel:** Zapewnienie, że każdy użytkownik ma ustawioną przynajmniej obowiązkową nazwę wyświetlaną (`username`).

*   **[x] Wykrywanie Nowego Użytkownika:** `AuthViewModel` po zalogowaniu sprawdza, czy `username` jest pusty.
*   **[x] Przekierowanie do Formularza:** Aplikacja poprawnie nawiguje do `SetupProfileScreen`.
*   **[x] Ekran `SetupProfileScreen`:** Stworzono formularz z polami `username`, `firstName`, `lastName`.
*   **[x] Logika Zapisu:** `AuthViewModel` i `AuthRepository` obsługują walidację unikalności `username` i zapisują dane w Firestore, a następnie nawigują do `MainScreen`.

### Krok 2: Ekran Społeczności - Wyszukiwanie i Zapraszanie [W TOKU]

**Cel:** Umożliwienie użytkownikom wyszukiwania innych graczy i wysyłania im zaproszeń do znajomych.

*   **[x] Stworzenie Struktury:** Zaimplementowano `CommunityViewModel`, `CommunityRepository` (interfejs i implementacja) oraz podłączono je przez Hilt.
*   **[x] UI Ekranu Społeczności:** Stworzono podstawowy layout `CommunityScreen` z polem wyszukiwania.
*   **[x] Implementacja Wyszukiwarki Graczy:** Logika wyszukiwania w `CommunityRepository` działa i zwraca wyniki z Firestore.
*   **[x] Wyświetlanie Wyników Wyszukiwania:** `CommunityScreen` wyświetla listę znalezionych użytkowników.
*   **[x] Implementacja Wysyłania Zaproszeń:** Logika `sendFriendRequest` w repozytorium i `ViewModelu` jest gotowa. Przycisk "Dodaj" przy wyniku wyszukiwania poprawnie wysyła zaproszenie.
*   **[DO ZROBIENIA] Wyświetlanie Otrzymanych Zaproszeń:**
    *   **Zadanie:** W `CommunityViewModel` stworzyć `StateFlow` przechowujący listę otrzymanych zaproszeń.
    *   **Zadanie:** W `CommunityScreen` zaimplementować `LazyColumn` w sekcji "Zaproszenia do znajomych", która będzie wyświetlać te zaproszenia.
*   **[DO ZROBIENIA] Wyświetlanie Listy Znajomych:**
    *   **Zadanie:** W `CommunityViewModel` stworzyć `StateFlow` przechowujący listę znajomych.
    *   **Zadanie:** W `CommunityScreen` zaimplementować `LazyColumn` w sekcji "Twoi znajomi".

### Krok 3: Zarządzanie Zaproszeniami i Znajomymi

**Cel:** Umożliwienie akceptowania/odrzucania zaproszeń i usuwania znajomych.

*   **[DO ZROBIENIA] Akceptowanie Zaproszenia:**
    *   **Zadanie:** Dodać przycisk "Akceptuj" przy każdym zaproszeniu.
    *   **Zadanie:** Zaimplementować logikę `acceptFriendRequest` w `CommunityRepository` i `CommunityViewModel`.
*   **[DO ZROBIENIA] Odrzucanie Zaproszenia:**
    *   **Zadanie:** Dodać przycisk "Odrzuć" przy każdym zaproszeniu.
    *   **Zadanie:** Zaimplementować logikę `rejectFriendRequest` w `CommunityRepository` i `CommunityViewModel`.
*   **[DO ZROBIENIA] Usuwanie Znajomego:**
    *   **Zadanie:** Dodać opcję usunięcia znajomego (np. na jego profilu lub przez długie naciśnięcie na liście).
    *   **Zadanie:** Zaimplementować logikę `removeFriend` w `CommunityRepository` i `CommunityViewModel`.

### Krok 4: Ekran Profilu i Prywatność

**Cel:** Stworzenie publicznej wizytówki gracza oraz miejsca do edycji własnych danych.

*   **[DO ZROBIENIA] Widok Profilu:**
    *   **Zadanie:** Zaprojektować UI dla `ProfileScreen`, które będzie wyświetlać dane użytkownika.
*   **[DO ZROBIENIA] Edycja Profilu:**
    *   **Zadanie:** Stworzyć nowy ekran `EditProfileScreen` z formularzem do edycji `username`, `firstName`, `lastName`, `club` i `profileImageUrl`.
*   **[DO ZROBIENIA] Ustawienia Prywatności:**
    *   **Zadanie:** W `EditProfileScreen` dodać przełącznik do zmiany flagi `isRealNameVisible`.
    *   **Zadanie:** W `ProfileScreen` (widok publiczny) zaimplementować logikę, która ukrywa imię i nazwisko, jeśli flaga jest ustawiona na `false`.
