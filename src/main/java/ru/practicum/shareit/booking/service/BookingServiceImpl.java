package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.exception.BookingErrorException;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final UserService userService;
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final ItemService itemService;

    @Override
    public BookingOutDto createBooking(Long userId, BookingInDto bookingInDto)
            throws UserNotFoundException, ItemNotFoundException, BookingErrorException {
        userService.checkUserExist(userId);
        ItemDto itemDto = itemService.findItemById(bookingInDto.getItemId());
        if (!itemDto.getAvailable()) {
            throw new BookingErrorException("Item not available.");
        }
        if (userId.equals(itemDto.getOwner())) {
            throw new ItemNotFoundException("Can't booking yourself item.");
        }
        checkCorrectDateTime(bookingInDto);
        Booking booking = bookingMapper.dtoToBooking(bookingInDto);
        booking.setStatus(BookingStatus.WAITING);
        booking.setBookerId(userId);
        booking = bookingRepository.save(booking);

        return getBookingOutDtoWithItemAndUser(booking);
    }

    private void checkCorrectDateTime(BookingInDto bookingInDto) throws BookingErrorException {
        if (bookingInDto.getStart().isBefore(LocalDateTime.now())) {
            throw new BookingErrorException("Wrong start date.");
        }
        if (bookingInDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new BookingErrorException("Wrong end date.");
        }
        if (bookingInDto.getEnd().isBefore(bookingInDto.getStart())) {
            throw new BookingErrorException("Wrong date end before start.");
        }
    }

    @Override
    public BookingOutDto updateBookingApproveStatus(Long userId, Long bookingId, String bookingStatus)
            throws UserNotFoundException, BookingNotFoundException, ItemNotFoundException, BookingErrorException {
        userService.checkUserExist(userId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking ID not found."));
        checkItemOwnerForAccess(userId, booking);
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BookingErrorException("Status already set");
        }

        if ("true".equals(bookingStatus)) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        booking = bookingRepository.save(booking);

        return getBookingOutDtoWithItemAndUser(booking);
    }

    private void checkItemOwnerForAccess(Long userId, Booking booking) throws ItemNotFoundException, BookingNotFoundException {
        ItemDto itemDto = itemService.findItemById(booking.getItemId());
        if (!userId.equals(itemDto.getOwner())) {
            throw new BookingNotFoundException("Access denied.");
        }
    }

    @Override
    public BookingOutDto findBookingById(Long userId, Long bookingId)
            throws UserNotFoundException, BookingNotFoundException, ItemNotFoundException {
        userService.checkUserExist(userId);
        checkBookingExist(bookingId);
        Booking booking = bookingRepository.findByIdAndBookerOrOwner(bookingId, userId)
                .orElseThrow(() -> new BookingNotFoundException("Booking for your userId not found."));

        return getBookingOutDtoWithItemAndUser(booking);
    }

    @Override
    public List<BookingOutDto> findAllBookingByUserAndState(Long userId, String state)
            throws UserNotFoundException, ItemNotFoundException, BookingErrorException {
        checkStateValue(state);
        userService.checkUserExist(userId);
        List<Booking> bookingList = new ArrayList<>();
        switch (state) {
            case ("ALL"):
                bookingList = bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
                break;
            case ("CURRENT"):
                bookingList = bookingRepository.findAllByBookerByDateIntoPeriodOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case ("PAST"):
                bookingList = bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case ("FUTURE"):
                bookingList = bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case ("REJECTED"):
                bookingList = bookingRepository.findAllByBookerAndStatusRejectedOrderByStartDesc(userId);
                break;
            case ("WAITING"):
                bookingList = bookingRepository.findAllByBookerAndStatusWaitingOrderByStartDesc(userId);
                break;
            default:
        }
        return bookingListToOutDtoList(bookingList);
    }

    private void checkStateValue(String state) throws BookingErrorException {
        List<String> stateList = List.of("ALL", "CURRENT", "PAST", "FUTURE", "REJECTED", "WAITING");
        if (!stateList.contains(state)) {
            throw new BookingErrorException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public List<BookingOutDto> findAllBookingByOwnerAndState(Long ownerId, String state) throws BookingErrorException, UserNotFoundException, ItemNotFoundException {
        checkStateValue(state);
        userService.checkUserExist(ownerId);
        List<Booking> bookingList = new ArrayList<>();
        switch (state) {
            case ("ALL"):
                bookingList = bookingRepository.findAllByItemOwnerOrderByStartDesc(ownerId);
                break;
            case ("CURRENT"):
                bookingList = bookingRepository.findAllByItemOwnerByDateIntoPeriodOrderByStartDesc(ownerId, LocalDateTime.now());
                break;
            case ("PAST"):
                bookingList = bookingRepository.findAllByItemOwnerAndEndIsBeforeOrderByStartDesc(ownerId, LocalDateTime.now());
                break;
            case ("FUTURE"):
                bookingList = bookingRepository.findAllByItemOwnerAndStartIsAfterOrderByStartDesc(ownerId, LocalDateTime.now());
                break;
            case ("REJECTED"):
                bookingList = bookingRepository.findAllByItemOwnerAndStateRejectedOrderByStartDesc(ownerId);
                break;
            case ("WAITING"):
                bookingList = bookingRepository.findAllByItemOwnerAndStateWaitingOrderByStartDesc(ownerId);
                break;
            default:
        }
        return bookingListToOutDtoList(bookingList);
    }

    @Override
    public List<BookingOutDto> findAllBookingByOwnerIdAndItemId(Long ownerId, Long itemId)
            throws UserNotFoundException, ItemNotFoundException {
        return bookingListToOutDtoList(bookingRepository.findAllByItemOwnerAndItemIdOrderByStartAsc(ownerId, itemId));
    }

    @Override
    public List<Booking> findAllBookingByUserIdAndItemId(Long userId, Long itemId, LocalDateTime dateTime) {
        return bookingRepository.findAllByItemUserIdAndItemIdOrderByStartDesc(userId, itemId, dateTime);
    }

    private List<BookingOutDto> bookingListToOutDtoList(List<Booking> bookingList)
            throws UserNotFoundException, ItemNotFoundException {
        List<BookingOutDto> bookingOutDtoList = new ArrayList<>();
        for (Booking booking : bookingList) {
            bookingOutDtoList.add(getBookingOutDtoWithItemAndUser(booking));
        }
        return bookingOutDtoList;
    }

    private BookingOutDto getBookingOutDtoWithItemAndUser(Booking booking)
            throws ItemNotFoundException, UserNotFoundException {
        BookingOutDto bookingOutDto = bookingMapper.bookingToDto(booking);
        bookingOutDto.setItem(itemService.findItemById(booking.getItemId()));
        bookingOutDto.setBooker(userService.findUserById(booking.getBookerId()));
        return bookingOutDto;
    }

    private void checkBookingExist(Long bookingId) throws BookingNotFoundException {
        bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException("Booking ID not found."));
    }
}