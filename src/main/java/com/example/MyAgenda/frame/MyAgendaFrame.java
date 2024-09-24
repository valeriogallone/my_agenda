package com.example.MyAgenda.frame;

import com.example.MyAgenda.model.Event;
import com.example.MyAgenda.repository.MyAgendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Component
public class MyAgendaFrame extends JFrame {

    private JPanel calendarPanel;
    private JPanel eventPanel;
    private JLabel month;
    private JButton prevButton;
    private JButton nextButton;
    private YearMonth currentMonth;
    private LocalDate selectedDate;

    @Autowired
    private MyAgendaRepository repository;

    @Autowired
    public MyAgendaFrame() {
        currentMonth = YearMonth.now();
    }

    public void initialize() {
        setTitle("Calendar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        JPanel headerPanel = new JPanel(new BorderLayout());
        month = new JLabel("", SwingConstants.CENTER);
        prevButton = new JButton("<");
        nextButton = new JButton(">");

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentMonth = currentMonth.minusMonths(1);
                updateCalendar();
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentMonth = currentMonth.plusMonths(1);
                updateCalendar();
            }
        });

        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(month, BorderLayout.CENTER);
        headerPanel.add(nextButton, BorderLayout.EAST);

        calendarPanel = new JPanel(new GridBagLayout());
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        eventPanel = new JPanel();
        eventPanel.setLayout(new BoxLayout(eventPanel, BoxLayout.Y_AXIS));
        eventPanel.setPreferredSize(new Dimension(200, getHeight()));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(calendarPanel, BorderLayout.CENTER);
        mainPanel.add(eventPanel, BorderLayout.EAST);

        add(mainPanel);

        updateCalendar();
        setVisible(true);
    }

    private void updateCalendar() {
        calendarPanel.removeAll();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        month.setText(currentMonth.format(monthFormatter).toUpperCase());

        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        int dayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue() % 7;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Thu", "Wed", "Fri", "Sat"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            gbc.gridx = i;
            gbc.gridy = 0;
            JLabel dayLabel = new JLabel(daysOfWeek[i], SwingConstants.CENTER);

            if ("Sat".equals(daysOfWeek[i])) {
                dayLabel.setForeground(Color.BLUE);
            } else if ("Sun".equals(daysOfWeek[i])) {
                dayLabel.setForeground(Color.RED);
            }
            calendarPanel.add(dayLabel, gbc);
        }

        int dayCounter = dayOfWeekValue;
        for (int i = 0; i < dayOfWeekValue; i++) {
            gbc.gridx = i;
            gbc.gridy = 1;
            calendarPanel.add(new JLabel(""), gbc);
        }

        LocalDate today = LocalDate.now();
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            gbc.gridx = dayCounter % 7;
            gbc.gridy = (dayCounter / 7) + 1;

            LocalDate date = currentMonth.atDay(day);
            JLabel dayLabel;
            if (currentMonth.getYear() == today.getYear() && currentMonth.getMonthValue() == today.getMonthValue() && day == today.getDayOfMonth()) {
                dayLabel = new CircleLabel(String.valueOf(day));
            } else {
                dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
                if (gbc.gridx == 0) {
                    dayLabel.setForeground(Color.RED);
                } else if (gbc.gridx == 6) {
                    dayLabel.setForeground(Color.BLUE);
                }
            }

            dayLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedDate = date;
                    updateEventPanel(date);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    dayLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            });

            calendarPanel.add(dayLabel, gbc);

            if (hasEvent(date)) {
                addGreenDot(dayLabel);
            }

            dayCounter++;
        }

        validate();
        repaint();
    }

    private boolean hasEvent(LocalDate date) {
        List<Event> events = repository.findByDate(date);
        return !events.isEmpty();
    }

    private void addGreenDot(JLabel label) {
        label.setText(label.getText() + " ");
        label.setIcon(createGreenCircle());
    }

    private void updateEventPanel(LocalDate date) {
        eventPanel.removeAll();
        List<Event> events = repository.findByDate(date);
        events.sort(Comparator.comparing(Event::getHour));

        if (events.isEmpty()) {
            eventPanel.add(new JLabel("No event for " + date));
            eventPanel.add(Box.createVerticalStrut(10));
            eventPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 5, 0));

        } else {
            for (Event event : events) {
                JLabel eventLabel = new JLabel(event.getHour() + ": " + event.getDescription());
                eventPanel.add(eventLabel);
                eventPanel.add(Box.createVerticalStrut(5));
                eventPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 5, 0));
                eventLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

                JButton modifyEventButton = new JButton("Modify Event");
                modifyEventButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        modifyEvent(event);
                    }
                });
                eventPanel.add(modifyEventButton);
                eventPanel.add(Box.createVerticalStrut(5));

                JButton deleteEventButton = new JButton("Delete Event");
                deleteEventButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        deleteEvent(event, date);
                    }
                });
                eventPanel.add(deleteEventButton);
                eventPanel.add(Box.createVerticalStrut(30));
            }
        }

        JButton addEventButton = new JButton("New Event");
        addEventButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String eventDescription = JOptionPane.showInputDialog(MyAgendaFrame.this, "New Event for " + date);
                if (eventDescription != null && !eventDescription.isEmpty()) {
                    String time = JOptionPane.showInputDialog(MyAgendaFrame.this, "Enter the time (HH:MM)");
                    if (time != null && !time.isEmpty()) {
                        Event newEvent = new Event(date, eventDescription, time);
                        repository.save(newEvent);
                        updateEventPanel(date);
                        updateCalendar();
                    }
                }
            }
        });
        eventPanel.add(addEventButton);
        eventPanel.add(Box.createVerticalStrut(5));

        validate();
        repaint();
    }

    private void modifyEvent(Event event) {
        String newDescription = JOptionPane.showInputDialog(MyAgendaFrame.this, "Modify description:", event.getDescription());
        if (newDescription != null && !newDescription.isEmpty()) {
            String newTime = JOptionPane.showInputDialog(MyAgendaFrame.this, "Modify time (HH:MM):", event.getHour());
            if (newTime != null && !newTime.isEmpty()) {
                event.setDescription(newDescription);
                event.setHour(newTime);
                repository.save(event);
                updateEventPanel(selectedDate);
                updateCalendar();
            }
        }
    }

    private void deleteEvent(Event event, LocalDate date) {
        int option = JOptionPane.showConfirmDialog(MyAgendaFrame.this,
                "Do you want to delete the event?",
                "Conferm cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            repository.delete(event);
            updateEventPanel(date);
            updateCalendar();
        }
    }

    private Icon createGreenCircle() {
        int size = 10;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.GREEN);
        g2d.fillOval(0, 0, size, size);
        g2d.dispose();
        return new ImageIcon(image);
    }

    private static class CircleLabel extends JLabel {
        public CircleLabel(String text) {
            super(text, SwingConstants.CENTER);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.YELLOW);
            int diameter = Math.min(getWidth(), getHeight()) - 10;
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;
            g2.fillOval(x, y, diameter, diameter);
            super.paintComponent(g);
        }
    }
}