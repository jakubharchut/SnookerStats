# Specyfikacja Projektu: Aplikacja "Snooker Stats"

## Wersja: 1.9 (stan na 2025-11-14)

---

## 1. Wizja i Cel Główny

Stworzenie zaawansowanej, społecznościowej platformy mobilnej dla amatorów snookera. Aplikacja ma służyć nie tylko jako cyfrowy notatnik do zapisywania wyników, ale przede wszystkim jako inteligentne narzędzie do głębokiej analizy statystyk, śledzenia postępów treningowych, rywalizacji z innymi graczami oraz organizacji amatorskich turniejów. Celem jest zbudowanie zaangażowanej społeczności i dostarczenie narzędzia, które realnie pomaga graczom w rozwoju swoich umiejętności.

---

## 2. Architektura i Fundament Technologiczny

### 2.1. Platforma Backendowa: Google Firebase
Aplikacja będzie w pełni oparta o ekosystem Google Firebase, co zapewnia skalowalność, funkcje czasu rzeczywistego i bezpieczeństwo.
*   **Baza Danych:** **Cloud Firestore** jako główne i jedyne źródło prawdy. Będzie przechowywać wszystkie dane użytkowników, mecze, statystyki, turnieje, itp. Jej mechanizmy real-time (`addSnapshotListener`) są kluczowe dla funkcji społecznościowych i zapewniają natychmiastową aktualizację danych w UI.
*   **Uwierzytelnianie:** **Firebase Authentication** do zarządzania kontami użytkowników (rejestracja, logowanie przez e-mail/hasło, dostawców społecznościowych jak Google).
*   **Powiadomienia Push:** **Firebase Cloud Messaging (FCM)** do wysyłania powiadomień, np. o nowych zaproszeniach do znajomych. Logika jest obsługiwana przez **Cloud Functions**.
*   **Weryfikacja E-mail:** Po rejestracji, na adres użytkownika automatycznie wysyłany jest link weryfikacyjny. Dostęp do pełnej funkcjonalności aplikacji będzie możliwy dopiero po potwierdzeniu adresu e-mail.
*   **Akceptacja Regulaminu:** Proces rejestracji będzie wymagał od użytkownika aktywnego zaznaczenia zgody na regulamin serwisu. Przycisk rejestracji pozostanie nieaktywny do momentu wyrażenia zgody.
*   **Zapamiętywanie Sesji Logowania:** Aplikacja oferuje opcję "Zapamiętaj mnie" (Checkbox w `LoginScreen`), która pozwala na zapisanie e-maila i hasła w bezpiecznym miejscu na urządzeniu (Encrypted SharedPreferences). Po zaznaczeniu tej opcji i udanym logowaniu, dane uwierzytelniające są zapisywane i automatycznie wypełniane przy kolejnych uruchomieniach aplikacji. Odznaczenie tej opcji usunie wcześniej zapisane dane. Wylogowanie użytkownika nie usuwa zapisanych danych.

### 2.2. Model Danych: "Online-First"
Aplikacja jest projektowana z myślą o stałym dostępie do internetu. Wszystkie operacje zapisu i odczytu danych docelowo kierowane są do chmury, co zapewnia spójność danych na wszystkich urządzeniach użytkownika.

### 2.3. Wsparcie Offline: Lokalna Baza Danych Room
Lokalna baza danych **Room** będzie pełnić rolę **pamięci podręcznej (cache)**, a nie głównego źródła danych.
*   **Cel:** Zapewnienie błyskawicznego działania interfejsu (odczyt z lokalnej kopii), możliwość przeglądania danych bez połączenia z internetem oraz możliwość zapisania meczu w trybie offline.
*   **Powrót do Gry:** Room będzie przechowywać stan bieżącego, niedokończonego meczu, aby użytkownik mógł do niego wrócić po przypadkowym zamknięciu aplikacji.

---

## 3. Kluczowe Funkcjonalności

### 3.1. Zarządzanie Profilem i Społeczność
*   **Szczegółowa Specyfikacja:** Dokładny opis modeli danych, przepływów i logiki dla tego modułu znajduje się w osobnym dokumencie: `SPOLECZNOSC_I_PROFIL.md`.
*   **Stopniowe Wdrażanie (Progressive Onboarding):** Po pierwszej, uproszczonej rejestracji (tylko e-mail i hasło), użytkownik jest przekierowywany do ekranu `SetupProfileScreen` w celu jednorazowego uzupełnienia profilu. Wszystkie pola (`username`, `firstName`, `lastName`) są **obowiązkowe**.
*   **Profile Graczy i Prywatność:**
    *   Każdy użytkownik posiada profil z możliwością ustawienia go jako **publiczny** lub **prywatny**.
    *   **Widoczność Profilu Publicznego:** Dostępny dla wszystkich użytkowników, łącznie ze statystykami.
    *   **Widoczność Profilu Prywatnego:** Dla nieznajomych widoczne są tylko podstawowe informacje (awatar, nazwa, imię i nazwisko). Pełne dane, w tym statystyki, są widoczne **tylko dla zaakceptowanych znajomych**.
    *   Właściciel profilu zawsze widzi swoje statystyki, niezależnie od ustawień prywatności.
*   **Ekran Profilu Użytkownika (`UserProfileScreen`):**
    *   Ekran został gruntownie przebudowany. Nie posiada `TopAppBar`, a jedynie przycisk "Cofnij" zintegrowany z layoutem.
    *   Zawiera **dynamiczne przyciski akcji**, które zmieniają się w zależności od statusu relacji (np. "Dodaj do znajomych", "Akceptuj/Odrzuć", "Anuluj zaproszenie", "Usuń znajomego").
    *   Na profilach innych graczy znajduje się karta **"Interakcje"** z przyciskiem **"Rozpocznij mecz"**.
    *   Opcja wysłania wiadomości jest dostępna dla wszystkich użytkowników, niezależnie od statusu znajomości.
*   **Ekran Zarządzania Profilem (`ManageProfileScreen`):**
    *   Na własnym profilu, przycisk "Zarządzaj profilem" prowadzi do dedykowanego ekranu.
    *   Ekran ten docelowo będzie zawierał wszystkie opcje konfiguracyjne. Obecnie zaimplementowano możliwość zmiany statusu profilu (publiczny/prywatny) za pomocą przełącznika `Switch`.
*   **System Sparing Partnerów ("Znajomych"):** Możliwość wysyłania, akceptowania, odrzucania i anulowania zaproszeń oraz usuwania znajomych została w pełni zaimplementowana z poziomu ekranu profilu oraz zakładki "Społeczność".
*   **System Komunikacji (Czat):**
    *   Aplikacja będzie zawierać system wiadomości prywatnych między użytkownikami. Główny dostęp do czatów będzie możliwy przez dedykowaną ikonę w `TopAppBar`.
    *   Możliwość zainicjowania nowej rozmowy będzie dostępna z poziomu listy znajomych oraz profilu innego użytkownika.
    *   **Szczegółowa Specyfikacja:** Dokładny opis architektury i UI dla tego modułu znajduje się w osobnym dokumencie: `system_komunikacji.md`.
*   **Kluby:** Funkcjonalność tworzenia i dołączania do grup (klubów), które posiadają własne, wewnętrzne rankingi i statystyki.

### 3.2. Rejestrowanie Meczy
*   **Dwa Tryby Gry:**
    1.  **Mecz Online (Live):** Rozgrywka w czasie rzeczywistym ze sparing partnerem. Obaj gracze korzystają z współdzielonego ekranu do wprowadzania wyników uderzenie po uderzeniu.
    2.  **Mecz Lokalny (Solo lub z Gościem):** Możliwość samodzielnego wprowadzenia wyników meczu rozegranego offline.
*   **Kategoryzacja Meczu:** Każdy mecz musi być oznaczony jako **Rankingowy** (liczony do oficjalnych statystyk) lub **Sparingowy** (towarzyski).
*   **Współdzielona Historia:** Wynik meczu automatycznie pojawia się w historii obu graczy (jeśli obaj są użytkownikami aplikacji).
*   **Ukrywanie Meczów:** Każdy użytkownik ma możliwość "ukrycia" dowolnego meczu w swojej historii. Mecz taki znika tylko z jego widoku, pozostając widocznym dla przeciwnika. Jest to realizowane przez dodanie ID użytkownika do listy `hiddenFor` w dokumencie meczu.
*   **Format Gry:** Przed rozpoczęciem meczu użytkownik będzie mógł wybrać liczbę czerwonych bil (15, 10, 6, 3).
*   **Historia Uderzeń:** Aplikacja będzie zapisywać każde uderzenie w meczu (bila, czas, punkty), aby umożliwić szczegółową analizę i odtwarzanie przebiegu gry.

### 3.3. Moduł Turniejów
*   **Tworzenie Turniejów:** Użytkownik (organizator) może stworzyć nowy turniej, definiując jego nazwę, format i datę.
*   **Zarządzanie Uczestnikami:** Organizator może zapraszać uczestników, zarówno **użytkowników aplikacji** z listy znajomych, jak i **graczy gościnnych** (spoza aplikacji) poprzez wpisanie ich imienia.
*   **Automatyczna Drabinka:** Aplikacja automatycznie generuje i wizualizuje drabinkę turniejową (np. w systemie pucharowym).
*   **Aktualizacje na Żywo:** Organizator wprowadza wyniki poszczególnych meczy, a drabinka aktualizuje się w czasie rzeczywistym dla wszystkich uczestników.

### 3.4. Moduł Treningowy
*   **Dedykowana sekcja** z predefiniowanymi ćwiczeniami (np. "Line-up", wbijanie długich bil, trening odstaw).
*   **Zapis Sesji:** Każda sesja treningowa jest zapisywana, a postępy dla danego ćwiczenia są wizualizowane na **wykresie liniowym w czasie**.

### 3.5. Analiza i Statystyki
*   **Głęboka Analiza "Drill-Down":** Umożliwia nawigację od ogólnych statystyk, przez listę konkretnych meczy, aż po szczegółową analizę pojedynczego frejma.
*   **Wizualizacja Gry:** Dla każdego meczu i frejma dostępny będzie **histogram punktowy**, pokazujący przebieg gry uderzenie po uderzeniu.
*   **Zaawansowane Statystyki Brejków:** Aplikacja będzie zliczać i analizować breki w progach (20+, 30+, 50+, 100+).

### 3.6. Rywalizacja i Grywalizacja
*   **Porównania Head-to-Head:** Specjalny ekran do bezpośredniego porównania swoich statystyk z wybranym sparing partnerem.
*   **Rozbudowane Rankingi:** System rankingów z możliwością filtrowania według zasięgu (znajomi, klub, globalnie), konkretnej statystyki oraz okresu czasowego.
*   **Osiągnięcia, Odznaki i Puchary:**
    *   System automatycznie przyznawanych **odznak** za osiąganie kamieni milowych (np. "Pierwszy brejk 100+").
    *   Za wygranie turnieju zwycięzca otrzymuje na swoim profilu **specjalny puchar/trofeum**.

---

## 4. Wygląd i Interfejs Użytkownika (UI/UX)

### 4.1. Motyw Przewodni: "Light Mode First"
Aplikacja będzie oparta o jasny, czysty i profesjonalny wygląd, z opcją dodania trybu ciemnego w przyszłości.
*   **Tło:** Białe lub bardzo jasnoszare.
*   **Tekst:** Ciemnoszary (grafitowy).
*   **Kolor Akcentujący:** Stonowany, elegancki zielony (kolor sukna snookerowego) dla elementów interaktywnych.
*   **Kolor Wyróżniający:** Złoty/żółty dla odznak, trofeów, rekordów i osiągnięć.

### 4.2. Spójna Struktura Ekranów
*   **Wykorzystanie `Scaffold`:** Każdy główny ekran w aplikacji będzie zbudowany w oparciu o komponent `Scaffold` z Jetpack Compose, aby zapewnić spójność.
*   **Górny Pasek Aplikacji (`TopAppBar`):** Zawiera tytuł aplikacji. Będzie również zawierał ikony akcji, takie jak **Wiadomości (Czat)**, **Profil** i **Wyloguj**.
*   **Dolny Pasek Nawigacyjny (`BottomNavigationBar`):** Główna nawigacja między kluczowymi sekcjami (Dashboard, Graj, Historia, Statystyki, Ludzie, Profil).
*   **Obszar Treści:** Centralna część ekranu, w której wyświetlana jest zawartość.
*   **Menu Zakładek (`TabRow`):** W przypadku złożonych ekranów (takich jak Społeczność czy Graj), do dalszej organizacji treści będzie używany system zakadek umieszczony pod `TopAppBar`. Szczegółowy opis tego wzorca znajduje się w odpowiedniej specyfikacji modułu.
*   **Potwierdzenia Akcji (Snackbar):** Zaimplementowano globalny `SnackbarManager`, który pozwala na wyświetlanie krótkich komunikatów na dole ekranu, potwierdzających wykonanie akcji (np. "Wysłano zaproszenie").

### 4.3. Priorytety Projektowe
*   **Czytelność i Intuicyjność:** Interfejs musi być prosty w obsłudze, nawet podczas stresującego meczu.
*   **Wizualizacja Danych:** Duży nacisk na atrakcyjne i zrozumiałe prezentowanie danych za pomocą wykresów i grafów.
*   **Modularność:** Dzięki `Scaffold`, zmiany w globalnych elementach UI będą łatwe do wprowadzenia bez naruszania struktury poszczególnych ekranów.

---

## 5. Model Monetyzacji

### 5.1. Wersja Darmowa
*   **Dostęp do Pełnej Funkcjonalności:** Użytkownicy mają dostęp do wszystkich funkcji aplikacji, w tym meczów online i turniejów.
*   **Wyświetlanie Reklam:** Wersja darmowa będzie zawierać dyskretne, nieinwazyjne reklamy (np. banery, reklamy pełnoekranowe po zakończeniu meczu).

### 5.2. Wersja Płatna ("Ad-Free")
*   **Jednorazowa Opłata:** Użytkownicy mogą dokonać niewielkiej, jednorazowej opłaty wewnątrz aplikacji.
*   **Usunięcie Reklam:** Zakup ten permanentnie usuwa wszystkie reklamy z aplikacji, zapewniając nieprzerwane doświadczenie.
*   **Odznaka Wspierającego:** Jako podziękowanie, użytkownicy, którzy dokonają zakupu, mogą otrzymać specjalną odznakę na swoim profilu.

---

## 6. Plan Realizacji Projektu (Roadmap)

### Etap 1: Fundamenty i Konfiguracja
- [x] Utworzenie projektu w konsoli Firebase.
- [x] Dodanie zależności do `build.gradle.kts` (Firebase, Hilt, Room, Compose Navigation, material-icons-extended, etc.).
- [x] Stworzenie bazowej struktury pakietów (`data`, `domain`, `ui`).
- [x] Podstawowa konfiguracja Hilt.

### Etap 2: Uwierzytelnianie Użytkownika
- [x] Zbudowanie UI dla ekranów Logowania i Rejestracji.
- [x] Stworzenie podstawowej nawigacji między ekranami autentykacji.
- [x] Stworzenie `AuthViewModel` do obsługi logiki.
- [x] Podłączenie logiki do Firebase Authentication (Rejestracja i Logowanie).
- [x] Implementacja wysyłania e-maila weryfikacyjnego.
- [x] Zablokowanie dostępu dla niezweryfikowanych użytkowników.
- [x] Stworzenie nawigacji po zalogowaniu (przekierowanie).

### Etap 3: Szkielet UI i Nawigacja Główna
- [x] Implementacja głównego ekranu z `Scaffold`.
- [x] Implementacja `BottomNavigationBar`, która umożliwia nawigację między głównymi sekcjami aplikacji.
- [x] **Refaktoryzacja ekranu "Graj":** Przepływ rozpoczynania meczu został ujednolicony. Ekran "Graj" zawiera teraz dynamiczne zakładki "Gracze", "Gość" i "Trening", których zawartość wyświetlana jest bezpośrednio, bez przechodzenia do osobnych ekranów.
- [x] Stworzenie pustych ekranów dla każdej sekcji.
- [x] Implementacja nawigacji do ekranu profilu oraz akcji wylogowania w `TopAppBar`.
- [x] W formularzu logowania (`LoginScreen.kt`) zaimplementowano obsługę klawiatury.
- [x] Zaimplementowano funkcję "Zapamiętaj mnie" (automatyczne wypełnianie formularza).

### Etap 4: Modele Danych i Baza Lokalna
- [x] **Zdefiniowanie Modeli Danych:** Stworzono klasy `data class` dla `User`, `Shot`, `Frame` i `Match`. Do klasy `Match` dodano pole `hiddenFor: List<String>` w celu obsługi ukrywania meczy.
- [x] **Konfiguracja Bazy Danych Room:**
    *   **Encje:** Klasy `User`, `Match` i `Frame` zostały oznaczone jako encje (`@Entity`).
    *   **Konwertery Typów (`TypeConverter`):** Stworzono `Converters.kt` do obsługi typów złożonych.
    *   **DAO (Data Access Objects):** Stworzono interfejsy `UserDao.kt` i `MatchDao.kt`.
    *   **Klasa Bazy Danych:** Stworzono główną klasę `SnookerStatsDatabase.kt` i podniesiono jej wersję w celu odzwierciedlenia zmian w schemacie.
    *   **Hilt Module dla Room:** Stworzono `DatabaseModule.kt` do dostarczania instancji bazy danych i DAO.

### Etap 5: Funkcje Społecznościowe
*   **Cel:** Implementacja kluczowych funkcji społecznościowych, które pozwolą użytkownikom na interakcję i budowanie sieci kontaktów w aplikacji.
*   **Status:** Główne funkcjonalności zostały zaimplementowane i są w fazie testów/dopracowywania.
*   **Zrealizowane zadania:**
    *   [x] **Wyszukiwarka graczy i profil publiczny.**
    *   [x] **System zaproszeń do znajomych.**
    *   [x] **Lista znajomych.**
    *   [x] **Zarządzanie profilem.**
*   **Pozostałe zadania w ramach etapu:**
    *   [ ] **Ekran porównania statystyk Head-to-Head.**
    *   [ ] **Implementacja systemu czatu.**

### Etap 6: Rdzeń Aplikacji - Zapis Meczu Lokalnego
*   **Cel:** Umożliwienie użytkownikom zapisywania wyników meczów lokalnie.
*   **Wymagania:** Zakończony Etap 4 (Modele Danych) i Etap 5 (Fundamenty społecznościowe).
*   **Zrealizowane zadania:**
    *   [x] UI ekranu wprowadzania wyniku (shot-by-shot).
    *   [x] ViewModel (`ScoringViewModel`) zarządzający stanem meczu.
    *   [x] Logika zapisu meczu do Room i Firestore, w tym pełna obsługa graczy-gości.

### Etap 7: Wyświetlanie Danych
- [x] Ekran historii meczy (`MatchHistoryScreen`) z poprawną obsługą graczy-gości.
- [x] Funkcja ukrywania meczy z poziomu historii.
- [x] Ekran szczegółów meczu (`MatchDetailsScreen`) z poprawną obsługą graczy-gości.
- [ ] Dashboard z podstawowymi statystykami.

### Etap 8: Mecz Online w Czasie Rzeczywistym
- [ ] Synchronizacja danych przy użyciu listenerów Firestore.
- [ ] System zapraszania do gry online.
- [ ] Obsługa przypadków brzegowych (np. utrata połączenia).

### Etap 9: Moduł Turniejów
- [ ] UI do tworzenia turnieju i zapraszania graczy.
- [ ] Logika generowania drabinki turniejowej.
- [ ] Interfejs do wprowadzania wyników i aktualizacji drabinki.

### Etap 10: Funkcje Zaawansowane i Grywalizacja
- [ ] Implementacja Modułu Treningowego (stworzono szkielet UI).
- [ ] System przyznawania Odznak, Osiągnięć i Pucharów.
- [ ] Zbudowanie rozbudowanych Rankingów.

### Etap 11: Implementacja Monetyzacji
- [ ] Integracja z Google AdMob i wyświetlanie reklam w wersji darmowej.
- [ ] Implementacja jednorazowego zakupu w aplikacji (In-App Purchase) w celu usunięcia reklam.

### Etap 12: Testowanie, Poprawki i Publikacja
- [ ] Testy jednostkowe i UI.
- [ ] Dopracowanie detali wizualnych i animacji.
- [ ] **Zabezpieczenie reguł Firebase:** Zmiana reguł dostępu do Firestore z trybu testowego na produkcyjny.
- [ ] Publikacja w sklepie Google Play.

---
## 7. Implementacja - Szczegóły Techniczne

### 7.1. Przepływ Rejestracji Użytkownika (`AuthViewModel`)
Logika rejestracji jest w pełni zamknięta w `AuthViewModel` i przebiega według następujących kroków:

1.  **Inicjacja:**
    *   `RegisterScreen` wywołuje publiczną funkcję `registerUser(email, password, confirmPassword)`.
2.  **Walidacja Danych:**
    *   Wewnątrz `ViewModelu`, przed wywołaniem Firebase, przeprowadzana jest walidacja.
3.  **Proces Rejestracji:**
    *   Jeśli walidacja przejdzie pomyślnie, `ViewModel` wystawia stan `State.Loading`.
    *   Wywoływana jest funkcja `repo.registerUser(...)`.
4.  **Obsługa Wyniku:**
    *   **Sukces:**
        *   Repozytorium tworzy użytkownika w Auth, wysyła e-mail weryfikacyjny i tworzy dokument w Firestore z pustym polem `username`.
        *   `ViewModel` emituje `NavigationEvent.NavigateToRegistrationSuccess`.
    *   **Błąd:**
        *   `ViewModel` wystawia stan `State.Error` z odpowiednim komunikatem.
5.  **Komunikacja z UI:**
    *   `RegisterScreen` obserwuje stan i zdarzenia nawigacyjne.

### 7.2. Przepływ Logowania Użytkownika (`AuthViewModel`)
Logika logowania jest w pełni zamknięta w `AuthViewModel` i przebiega według następujących kroków:
1.  **Inicjacja:**
    *   `LoginScreen` wywołuje publiczną funkcję `loginUser(email, password, rememberMe)`. W interfejsie użytkownika znajduje się `Checkbox` "Zapamiętaj mnie", którego stan (`rememberMe`) jest przekazywany do `ViewModelu`.
2.  **Walidacja Danych:**
    *   Wewnątrz `ViewModelu`, przed wywołaniem Firebase, przeprowadzana jest walidacja:
        *   Sprawdzenie, czy żadne z pól `email` i `password` nie jest puste.
    *   Jeśli walidacja nie powiedzie się, `ViewModel` wystawi odpowiedni stan błędu (np. `State.Error("Wszystkie pola muszą być wypełnione.")`), który UI będzie mogło wyświetlić użytkownikowi.
3.  **Proces Logowania:**
    *   Jeśli walidacja przejdzie pomyślnie, `ViewModel` wystawia stan `State.Loading`, aby UI mogło pokazać wskaźnik postępu (np. kółko).
    *   Wywoływana jest funkcja `repo.loginUser(...)`.
4.  **Obsługa Wyniku:**
    *   **Sukces:**
        *   Jeśli zadanie `signIn...` zakończy się sukcesem, `ViewModel` sprawdza, czy `firebaseAuth.currentUser?.isEmailVerified` jest `true`.
        *   **Obsługa "Zapamiętaj mnie":** Jeśli `rememberMe` jest `true`, `ViewModel` wywołuje odpowiednią funkcję w repozytorium, aby zapisać dane uwierzytelniające w bezpieczny sposób (np. w `Encrypted SharedPreferences`). Jeśli `rememberMe` jest `false`, wszelkie wcześniej zapisane dane są usuwane.
        *   Jeśli e-mail jest zweryfikowany, `ViewModel` emituje jednorazowe zdarzenie `NavigationEvent.NavigateToMain`.
        *   Jeśli e-mail nie jest zweryfikowany, `ViewModel` wystawia stan błędu `State.Error("Konto nie zostało zweryfikowane. Sprawdź e-mail.")`.
    *   **Błąd:**
        *   Jeśli zadanie się nie powiedzie (np. nieprawidłowe dane logowania, użytkownik nie istnieje, błąd sieci), `ViewModel` przechwytuje wyjątek z Firebase, mapuje go na zrozumiały komunikat (np. "Nieprawidłowy e-mail lub hasło.") i wystawia stan `State.Error`.
5.  **Komunikacja z UI:**
    *   Interfejs (`LoginScreen`) obserwuje stan (`State`) wystawiany przez `ViewModel` i reaguje na bieżąco, pokazując komunikaty o błędach, sukcesie lub wskaźnik ładowania.
    *   **Obsługa Nawigacji (Side-effects):**
        *   Do obsługi jednorazowych zdarzeń (jak nawigacja) używany jest `LaunchedEffect`, który nasłuchuje na `SharedFlow` z `ViewModelu`. Gwarantuje to, że nawigacja zostanie wywołana tylko raz.
        *   Po otrzymaniu zdarzenia `NavigateToMain`, wywoływana jest funkcja `navController.navigate("main")` ze specjalną opcją: `popUpTo("login") { inclusive = true }`.
        *   **Zasada Działania `popUpTo`:** Ten mechanizm czyści historię nawigacji (backstack). Usuwa wszystkie ekrany aż do ekranu `login` włącznie. W rezultacie, po pomyślnym zalogowaniu, użytkownik nie może wrócić do ekranu logowania za pomocą systemowego przycisku "wstecz", co jest kluczowe dla poprawnego UX.

### 7.3. Implementacja Etapu 3: Szkielet UI i Nawigacja Główna
Etap 3 został w pełni zrealizowany. Wprowadzono następujące elementy:
*   Główny ekran aplikacji (`MainScreen.kt`) został zbudowany w oparciu o komponent `Scaffold` z Jetpack Compose, zapewniając spójną ramę dla całej aplikacji.
*   Zaimplementowano `BottomNavigationBar`, która umożliwia nawigację między głównymi sekcjami aplikacji.
*   **Uproszczony przepływ gry:** Kliknięcie zakładek "Gość" lub "Trening" w sekcji "Graj" **bezpośrednio przenosi** użytkownika do ekranu konfiguracji meczu (`MatchSetupScreen`), pomijając zbędne ekrany pośrednie.
*   Dla każdej z głównych sekcji utworzono puste ekrany (`HomeScreen`, `PlayScreen`, `MatchHistoryScreen`, `StatsScreen`, `CommunityScreen`, `ProfileScreen`, `ChatListScreen`).
*   `TopAppBar` został zaimplementowany w `MainScreen.kt`. Zawiera dynamiczny tytuł (nazwę użytkownika), ikonę wiadomości (`Icons.Default.Message`) nawigującą do `ChatListScreen`, ikonę profilu (nawigującą do `ProfileScreen` za pomocą `internalNavController`) oraz ikonę wylogowania (nawigującą do ekranu `login` za pomocą `navController` z `MainActivity`, z opcją `popUpTo("login") { inclusive = true }` do czyszczenia stosu).
*   W formularzu logowania (`LoginScreen.kt`) obsługa klawisza `Tab` w polu e-mail oraz klawisza `Enter` w polu hasła została zaimplementowana za pomocą modyfikatora `onKeyEvent`, zapewniając płynne i oczekiwane działanie.
*   Zaimplementowano funkcję "Zapamiętaj mnie" (automatyczne wypełnianie formularza).

### 7.4. Implementacja Etapu 4: Modele Danych i Baza Lokalna (ZREALIZOWANO)

**Cel:** Stworzenie kompletnych modeli danych dla aplikacji oraz skonfigurowanie lokalnej bazy danych Room do ich przechowywania. Modele te stanowią fundament dla funkcji zapisu meczy, statystyk, turniejów i profili użytkowników, a ich implementacja została zakończona.

**A. Definiowanie Modeli Danych**

1.  **Klasa `User` (Użytkownik):**
    *   **Zadanie:** Rozszerzono klasę `User` w `domain/model/User.kt`.
    *   **Pola:** `uid: String`, `username: String`, `email: String`, `publicProfile: Boolean`, `club: String?`, `profileImageUrl: String?`, `friends: List<String>` (lista UID znajomych), `friendRequestsSent: List<String>`, `friendRequestsReceived: List<String>`.
    *   **Lokalizacja:** `domain/model/User.kt`.

2.  **Klasa `Shot` (Uderzenie):**
    *   **Zadanie:** Stworzono `data class Shot`.
    *   **Pola:** `timestamp: Long`, `ball: String` (np. "RED", "BLUE"), `points: Int`, `isFoul: Boolean`.
    *   **Lokalizacja:** `domain/model/Shot.kt`.

3.  **Klasa `Frame` (Frejm):**
    *   **Zadanie:** Stworzono `data class Frame`.
    *   **Pola:** `frameNumber: Int`, `player1Points: Int`, `player2Points: Int`, `shots: List<Shot>`.
    *   **Lokalizacja:** `domain/model/Frame.kt`.

4.  **Klasa `Match` (Mecz):**
    *   **Zadanie:** Stworzono `data class Match`.
    *   **Pola:** `id: String` (unikalny identyfikator), `player1Id: String`, `player2Id: String?` (opcjonalny, np. dla treningu solo, lub w formacie `guest_ImięGościa` dla graczy spoza systemu), `date: Long`, `matchType: String` (np. "RANKING", "SPARRING"), `numberOfReds: Int`, `status: String` (np. "IN_PROGRESS", "COMPLETED"), `frames: List<Frame>`, `hiddenFor: List<String>`.
    *   **Lokalizacja:** `domain/model/Match.kt`.

**B. Konfiguracja Bazy Danych Room**

1.  **Encje (`@Entity`):**
    *   **Zadanie:** Klasy `User`, `Match` i `Frame` zostały oznaczone jako encje Room (`@Entity`). Klasa `Shot` jest używana jako obiekt osadzony w `Frame` i konwertowana za pomocą `TypeConverter`.
    *   **Lokalizacja:** Pliki modeli danych zostały zaktualizowane.

2.  **Konwertery Typów (`TypeConverter`):**
    *   **Zadanie:** Stworzono klasę `Converters` (`data/local/Converters.kt`) z użyciem biblioteki Gson do serializacji/deserializacji `List<String>`, `List<Shot>` i `List<Frame>` na format JSON, co umożliwia ich przechowywanie w Room. Zależność `com.google.code.gson:gson` została dodana do projektu.
    *   **Lokalizacja:** `data/local/Converters.kt`.

3.  **DAO (Data Access Objects - `@Dao`):**
    *   **Zadanie:** Stworzono interfejsy `UserDao.kt` i `MatchDao.kt` (`data/local/dao`) z podstawowymi operacjami CRUD oraz zapytaniami do pobierania danych.
    *   **Lokalizacja:**
        *   `data/local/dao/UserDao.kt`
        *   `data/local/dao/MatchDao.kt`

4.  **Klasa Bazy Danych (`@Database`):**
    *   **Zadanie:** Stworzono główną klasę `SnookerStatsDatabase.kt` (`data/local`) z adnotacją `@Database`, deklaracją encji (`User`, `Match`, `Frame`) oraz podłączeniem `TypeConverter`.
    *   **Lokalizacja:** `data/local/SnookerStatsDatabase.kt`.

5.  **Hilt Module dla Room:**
    *   **Zadanie:** Stworzono `DatabaseModule.kt` w pakiecie `di`, aby Hilt mógł dostarczać instancje bazy danych i DAO.
    *   **Lokalizacja:** `di/DatabaseModule.kt`.
