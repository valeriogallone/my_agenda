package com.example.MyAgenda.repository;

import com.example.MyAgenda.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MyAgendaRepository extends JpaRepository<Event, Long> {
    List<Event> findByDate(LocalDate date);
}
