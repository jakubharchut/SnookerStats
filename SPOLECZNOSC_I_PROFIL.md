# Specyfikacja Modułu: Społeczność i Profil

## Wersja: 1.0 (stan na 2024-07-25)

---

## 1. Cel Główny

Celem tego modułu jest stworzenie w pełni funkcjonalnego systemu społecznościowego, który umożliwi użytkownikom interakcję, budowanie sieci kontaktów (znajomych) oraz zarządzanie swoim publicznym profilem. Moduł ten jest kluczowy dla budowania zaangażowania i umożliwienia rywalizacji między graczami.

---

## 2. Model Danych (User)

Centralnym modelem danych dla tego modułu jest klasa `User`. Będzie ona przechowywać wszystkie informacje o profilu i relacjach społecznościowych użytkownika.

**Definicja `data class User`:**
*   `uid: String` - (Klucz główny) Unikalny ID z Firebase Authentication.
*   `username: String` - **(Obowiązkowe)** Publiczna, unikalna nazwa wyświetlana, używana w wyszukiwarce, rankingach itp.
*   `email: String` - Adres e-mail (niepubliczny).
*   `firstName: String?` - **(Opcjonalne)** Prawdziwe imię użytkownika.
*   `lastName: String?` - **(Opcjonalne)** Prawdziwe nazwisko użytkownika.
*   `club: String?` - **(Opcjonalne)** Nazwa klubu, do którego należy gracz.
*   `profileImageUrl: String?` - **(Opcjonalne)** URL do zdjęcia profilowego.
*   `friends: List<String>` - Lista `uid` użytkowników, którzy są znajomymi.
*   `friendRequestsSent: List<String>` - Lista `uid` użytkowników, do których wysłano zaproszenie.
*   `friendRequestsReceived: List<String>` - Lista `uid` użytkowników, od których otrzymano zaproszenie.
*   `isRealNameVisible: Boolean` - Flaga prywatności. Jeśli `true`, `firstName` i `lastName` są widoczne dla innych. Domyślnie `false`.

---

## 3. Plan Implementacji Funkcjonalności

### Krok 1: Onboarding - Uzupełnienie Profilu po Rejestracji

**Cel:** Zapewnienie, że każdy użytkownik ma ustawioną przynajmniej obowiązkową nazwę wyświetlaną (`username`).

1.  **Wykrywanie Nowego Użytkownika:**
    *   Po pomyślnym zalogowaniu, aplikacja (w `AuthViewModel` lub podobnym miejscu) pobierze dane użytkownika z Firestore.
    *   Sprawdzi, czy pole `username` jest puste.
2.  **Przekierowanie do Formularza:**
    *   Jeśli `username` jest pusty, użytkownik zostanie przekierowany do nowego ekranu `SetupProfileScreen`.
    *   Jeśli `username` nie jest pusty, użytkownik zostanie przekierowany do głównego ekranu aplikacji (`MainScreen`).
3.  **Ekran `SetupProfileScreen`:**
    *   Będzie zawierał formularz z polami:
        *   `username` (Nazwa wyświetlana) - **pole obowiązkowe**.
        *   `firstName` (Imię) - pole opcjonalne.
        *   `lastName` (Nazwisko) - pole opcjonalne.
    *   Przycisk "Zapisz i kontynuuj".
4.  **Logika Zapisu:**
    *   Po kliknięciu przycisku, dane zostaną zwalidowane (czy `username` nie jest pusty).
    *   Aplikacja zaktualizuje dokument użytkownika w Firestore o nowe dane.
    *   Po pomyślnym zapisie, użytkownik zostanie przekierowany do `MainScreen`.

### Krok 2: Ekran Społeczności (`CommunityScreen`)

**Cel:** Stworzenie centralnego miejsca do zarządzania interakcjami społecznymi.

1.  **Wyszukiwarka Graczy:**
    *   Pole tekstowe do wpisywania `username`, `firstName` lub `lastName`.
    *   Wyniki wyszukiwania będą wyświetlane w czasie rzeczywistym w `LazyColumn`.
    *   Każdy wynik będzie zawierał `username`, opcjonalnie `firstName` i `lastName` (jeśli `isRealNameVisible` jest `true`) oraz przycisk "Dodaj do znajomych".
2.  **Lista Otrzymanych Zaproszeń:**
    *   Sekcja wyświetlająca listę użytkowników, którzy wysłali nam zaproszenie.
    *   Przy każdym zaproszeniu będą przyciski "Akceptuj" i "Odrzuć".
3.  **Lista Znajomych:**
    *   Sekcja wyświetlająca listę naszych aktualnych znajomych.
    *   Każdy element na liście będzie klikalny, prowadząc do profilu gracza.
4.  **Przycisk "Dodaj Gracza Gościnnego":**
    *   Otworzy prosty dialog do wpisania nazwy przeciwnika, który nie ma konta w aplikacji. Ta nazwa będzie przekazywana do ekranu rozpoczynania meczu.

### Krok 3: Implementacja Logiki Systemu Znajomych (`CommunityRepository`)

**Cel:** Stworzenie logiki backendowej do zarządzania relacjami.

1.  **Wysyłanie Zaproszenia (`sendFriendRequest`):**
    *   Akcja wykonywana po kliknięciu "Dodaj do znajomych".
    *   Aktualizuje dwa dokumenty w Firestore:
        *   Dodaje ID odbiorcy do `friendRequestsSent` u wysyłającego.
        *   Dodaje ID wysyłającego do `friendRequestsReceived` u odbiorcy.
2.  **Akceptowanie Zaproszenia (`acceptFriendRequest`):**
    *   Akcja wykonywana po kliknięciu "Akceptuj".
    *   Wykonuje transakcję w Firestore, która:
        *   Dodaje ID obu użytkowników do ich wzajemnych list `friends`.
        *   Usuwa ID z list `friendRequestsSent` i `friendRequestsReceived`.
3.  **Odrzucanie Zaproszenia (`rejectFriendRequest`):**
    *   Akcja wykonywana po kliknięciu "Odrzuć".
    *   Usuwa ID z list `friendRequestsSent` i `friendRequestsReceived`.
4.  **Usuwanie Znajomego (`removeFriend`):**
    *   Akcja dostępna na profilu znajomego.
    *   Usuwa ID obu użytkowników z ich wzajemnych list `friends`.

### Krok 4: Ekran Profilu Użytkownika (`ProfileScreen`)

**Cel:** Stworzenie publicznej wizytówki gracza oraz miejsca do edycji własnych danych.

1.  **Widok Publiczny:**
    *   Wyświetla `username`, `club`, `profileImageUrl`.
    *   Wyświetla `firstName` i `lastName` **tylko wtedy**, gdy `isRealNameVisible` jest `true`.
    *   Wyświetla statystyki, osiągnięcia itp. (zostaną zaimplementowane w przyszłości).
2.  **Widok Własnego Profilu:**
    *   Wyświetla te same dane, co widok publiczny.
    *   Dodatkowo zawiera przycisk "Edytuj profil", który pozwoli na zmianę `username`, `firstName`, `lastName`, `club`, `profileImageUrl` oraz flagi `isRealNameVisible`.
