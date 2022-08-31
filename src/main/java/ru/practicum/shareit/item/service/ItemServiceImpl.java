package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.exception.BookingErrorException;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.mapper.ItemWithBookingMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import java.nio.file.AccessDeniedException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final BookingService bookingService;
    private final ItemMapper itemMapper;
    private final ItemWithBookingMapper itemWithBookingMapper;

    @Autowired
    public ItemServiceImpl(UserService userService,
                           ItemRepository itemRepository,
                           @Lazy BookingService bookingService,
                           ItemMapper itemMapper,
                           ItemWithBookingMapper itemWithBookingMapper) {
        this.userService = userService;
        this.itemRepository = itemRepository;
        this.bookingService = bookingService;
        this.itemMapper = itemMapper;
        this.itemWithBookingMapper = itemWithBookingMapper;
    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) throws UserNotFoundException {
        if (itemDto.getAvailable() == null) {
            throw new InvalidParameterException("Value of parameter Available not found.");
        }
        checkBlankParameter(itemDto.getName());
        checkBlankParameter(itemDto.getDescription());
        userService.checkUserExist(userId);
        Item item = itemMapper.dtoToItem(itemDto);
        item.setOwner(userId);
        item = itemRepository.save(item);
        return itemMapper.itemToDto(item);
    }

    private static void checkBlankParameter(String value) {
        if (value == null || value.trim().isBlank()) {
            throw new InvalidParameterException("Value of parameter is blank or absent.");
        }
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto)
            throws
            UserNotFoundException,
            ItemNotFoundException,
            AccessDeniedException {
        if (itemDto.getName() != null) {
            checkBlankParameter(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            checkBlankParameter(itemDto.getDescription());
        }
        userService.checkUserExist(userId);
        Optional<Item> checkItem = itemRepository.findById(itemId);
        if (checkItem.isEmpty()) {
            throw new ItemNotFoundException("Item ID not found.");
        }
        if (!checkItem.get().getOwner().equals(userId)) {
            throw new AccessDeniedException("Other user access denied.");
        }
        Item updateItem = updateItemField(itemDto, checkItem);
        return itemMapper.itemToDto(itemRepository.save(updateItem));
    }

    private static Item updateItemField(ItemDto itemDto, Optional<Item> checkItem) {
        Item updateItem = checkItem.get();
        if (itemDto.getAvailable() != null) {
            updateItem.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getName() != null) {
            updateItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            updateItem.setDescription(itemDto.getDescription());
        }
        return updateItem;
    }

    @Override
    public ItemWithBookingDto findItemWithBookingById(Long userId, Long itemId)
            throws ItemNotFoundException, UserNotFoundException, BookingErrorException {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) {
            throw new ItemNotFoundException("Item ID not found.");
        }
        ItemWithBookingDto itemWithBookingDto = itemWithBookingMapper.itemToDto(item.get());
        if (userId == item.get().getOwner()) {
            List<BookingOutDto> bookingOutDtoList
                    = bookingService.findAllBookingByOwnerIdAndItemId(itemWithBookingDto.getOwner(), itemId);
            if (bookingOutDtoList.size() > 0) {
                itemWithBookingDto.setLastBooking(getBookingItemDto(bookingOutDtoList.get(0)));
            }
            if (bookingOutDtoList.size() > 1) {
                itemWithBookingDto.setNextBooking(getBookingItemDto(bookingOutDtoList.get(1)));
            }
        }
        return itemWithBookingDto;
    }

    private static BookingItemDto getBookingItemDto(BookingOutDto bookingOutDto) {
        return new BookingItemDto(bookingOutDto.getId(), bookingOutDto.getBooker().getId(),
                bookingOutDto.getStart(), bookingOutDto.getEnd(), bookingOutDto.getStatus());
    }

    @Override
    public ItemDto findItemById(Long itemId) throws ItemNotFoundException {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) {
            throw new ItemNotFoundException("Item ID not found.");
        }
        return itemMapper.itemToDto(item.get());
    }

    @Override
    public List<ItemWithBookingDto> findAllByUserId(Long userId) throws UserNotFoundException, BookingErrorException, ItemNotFoundException {
        userService.checkUserExist(userId);
        List<Item> itemList = itemRepository.findAllByOwner(userId);
        List<ItemWithBookingDto> itemWithBookingDtoList = new ArrayList<>();
        for (Item item : itemList) {
            itemWithBookingDtoList.add(findItemWithBookingById(userId, item.getId()));
        }
        return itemWithBookingDtoList;
    }

    @Override
    public List<ItemDto> findItemsByQueryText(String queryText) {
        if (queryText.trim().isBlank()) {
            return new ArrayList<>();
        }
        return itemMapper.itemListToDto(
                itemRepository.findItemByAvailableAndQueryContainWithIgnoreCase(queryText)
        );
    }
}