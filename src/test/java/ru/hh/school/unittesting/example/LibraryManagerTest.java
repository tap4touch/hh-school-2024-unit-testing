package ru.hh.school.unittesting.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hh.school.unittesting.homework.LibraryManager;
import ru.hh.school.unittesting.homework.NotificationService;
import ru.hh.school.unittesting.homework.UserService;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class LibraryManagerTest {
    private NotificationService notificationService;
    private UserService userService;
    private LibraryManager libraryManager;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        userService = mock(UserService.class);
        libraryManager = new LibraryManager(notificationService, userService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1984", "The Successor"})
    void testAddBook(String book) {
        libraryManager.addBook(book, 1);
        assertEquals(1, libraryManager.getAvailableCopies(book));

        libraryManager.addBook(book, 16);
        assertEquals(17, libraryManager.getAvailableCopies(book));
    }

    @Test
    void testBorrowBookActiveUser() {
        Mockito.when(userService.isUserActive("user")).thenReturn(true);
        libraryManager.addBook("1984", 3);

        boolean didTakeBook = libraryManager.borrowBook("1984", "user");

        assertTrue(didTakeBook);
        assertEquals(2, libraryManager.getAvailableCopies("1984"));
    }

    @Test
    void testBorrowBookNoBooks() {
        Mockito.when(userService.isUserActive("user")).thenReturn(true);

        boolean didTakeBook = libraryManager.borrowBook("User Story" ,"user");

        assertFalse(didTakeBook);
    }

    @Test
    void testBorrowBookInactiveUser() {
        Mockito.when(userService.isUserActive("user")).thenReturn(false);

        boolean didTakeBook = libraryManager.borrowBook("User Story" ,"user");

        assertFalse(didTakeBook);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1984", "The Successor"})
    void testReturnBookSuccess(String book) {
        Mockito.when(userService.isUserActive("user")).thenReturn(true);
        libraryManager.addBook(book, 3);
        libraryManager.borrowBook(book, "user");

        boolean didReturn = libraryManager.returnBook(book, "user");

        assertTrue(didReturn);
        assertEquals(3, libraryManager.getAvailableCopies(book));
    }

    @Test
    void testReturnBookWrongBook() {
        Mockito.when(userService.isUserActive("user")).thenReturn(true);
        libraryManager.addBook("1984", 3);
        libraryManager.borrowBook("1984", "user");

        boolean didReturn = libraryManager.returnBook("The Successor", "user");

        assertFalse(didReturn);
        assertEquals(2, libraryManager.getAvailableCopies("1984"));
    }

    @Test
    void testReturnBookWrongUser() {
        Mockito.when(userService.isUserActive("user1")).thenReturn(true);
        libraryManager.addBook("1984", 3);
        libraryManager.borrowBook("1984", "user1");

        boolean didReturn = libraryManager.returnBook("The Successor", "user2");

        assertFalse(didReturn);
        assertEquals(2, libraryManager.getAvailableCopies("1984"));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 7, 17, 99, 144, 313, 997, 1024})
    void testCalculateDynamicLateFee(int overdueDays) {
        double baseLateFeePerDay = 0.5;
        double bestsellerMultiplier = 1.5;
        double premiumMemberDiscount = 0.8;

        double fee = libraryManager.calculateDynamicLateFee(overdueDays, false, false);
        assertEquals(BigDecimal.valueOf(overdueDays*baseLateFeePerDay)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue(), fee);

        fee = libraryManager.calculateDynamicLateFee(overdueDays, true, false);
        assertEquals(BigDecimal.valueOf(overdueDays*baseLateFeePerDay*bestsellerMultiplier)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue(), fee);

        fee = libraryManager.calculateDynamicLateFee(overdueDays, false, true);
        assertEquals(BigDecimal.valueOf(overdueDays*baseLateFeePerDay*premiumMemberDiscount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue(), fee);

        fee = libraryManager.calculateDynamicLateFee(overdueDays, true, true);
        assertEquals(BigDecimal.valueOf(overdueDays*baseLateFeePerDay*bestsellerMultiplier*premiumMemberDiscount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue(), fee);
    }

    @Test
    void testCalculateDynamicLateFeeZeroDays() {
        double fee = libraryManager.calculateDynamicLateFee(0, false, false);
        assertEquals(0, fee);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1024, -906, -17, -1})
    void testCalculateDynamicLateFeeBelowZeroDays(int overdueDays) {
        assertThrows(IllegalArgumentException.class,
                () -> libraryManager.calculateDynamicLateFee(overdueDays, false, false));
    }
}
