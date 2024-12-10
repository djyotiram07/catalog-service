package com.polarbookshop.catalogservice.domain;

import com.polarbookshop.catalogservice.config.DataConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@Import(DataConfig.class)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@ActiveProfiles(value = "integration")
public class BookRepositoryJdbcTests {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @Test
    void findAllBooks() {
        var book1 = Book.of("1234567991", "Title", "Author", 10.99);
        var book2 = Book.of("1234567992", "Title", "Author", 11.99);
        jdbcAggregateTemplate.insert(book1);
        jdbcAggregateTemplate.insert(book2);
        Iterable<Book> actualBooks = bookRepository.findAll();

        assertThat(StreamSupport.stream(actualBooks.spliterator(), true)
                .filter(book -> book.isbn().equals(book1.isbn()) || book.isbn().equals(book2.isbn()))
                .collect(Collectors.toList())).hasSize(2);
    }

    @Test
    void findBookByIsbnWhenExisting() {
        var bookIsbn = "1234567791";
        var book = Book.of(bookIsbn, "Title", "Author", 12.90);
        jdbcAggregateTemplate.insert(book);
        Optional<Book> actualBook = bookRepository.findByIsbn(bookIsbn);

        assertThat(actualBook).isPresent();
        assertThat(actualBook.get().isbn()).isEqualTo(book.isbn());
    }

    @Test
    void findBookByIsbnWhenNotExisting() {
        Optional<Book> actualBook = bookRepository.findByIsbn("1234567891");
        assertThat(actualBook).isEmpty();
    }

    @Test
    void existsByIsbnWhenExisting() {
        var bookIsbn = "1234567891";
        var book = Book.of(bookIsbn, "Title", "Author", 20.99);
        jdbcAggregateTemplate.insert(book);
        boolean existing = bookRepository.existsByIsbn(bookIsbn);

        assertThat(existing).isTrue();
    }

    @Test
    void existsByIsbnWhenNotExisting() {
        boolean existing = bookRepository.existsByIsbn("1234567891");
        assertThat(existing).isFalse();
    }

    @Test
    void deleteByIsbn() {
        var bookIsbn = "1234567891";
        var bookToCreate = Book.of(bookIsbn, "Title", "Author", 10.99);
        var persistedBook = jdbcAggregateTemplate.insert(bookToCreate);
        bookRepository.deleteByIsbn(bookIsbn);

        assertThat(jdbcAggregateTemplate.findById(persistedBook.id(), Book.class)).isNull();
    }
}
