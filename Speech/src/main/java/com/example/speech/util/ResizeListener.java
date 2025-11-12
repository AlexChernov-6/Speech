package com.example.speech.util;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.List;

//Данный класс предназначен для изменения размеров окна, реализовывает интерфейс EventHandler
//Обработчик событий, события мыши, так-как изменения окна производятся именно ей
public class ResizeListener implements EventHandler<MouseEvent> {
    //Создаём переменную stage, которая будет заполнена через конструктор класса
    private final Stage stage;
    //Переменные, в которых будет храниться местоположение окна при нажатии мышки
    private double initialX;
    private double initialY;
    //Создадим переменные, в которых будет храниться исходные размеры окна
    private double initialWidth;
    private double initialHeight;
    //Создадим переменные, в которых будет храниться положение курсора в окне при нажатии
    private double mouseClickX;
    private double mouseClickY;

    //Создадим переменную, в которой будем хранить текущие положение курсора
    private ScreenFrame cursorPosition;

    //Задаём отступы, где будет доступно изменение размеров(13px окна приложения)
    private static final double BORDER = 13;
    //Задаём минимальные и максимальные размеры окна размеры окна
    private static final double MIN_WIDTH = 600;
    private static final double MIN_HEIGHT = 600;
    private static final double MAX_WIDTH = initializeMaxWidth();
    private static final double MAX_HEIGHT = initializeMaxHeight();

    //Создадим два статических метода, которые будут инициализировать константы максимальных размеров окна
    //Нельзя указать точное значение в px, так-как разрешения у пользователей могут быть разные
    private static double initializeMaxWidth() {
        //Из класса Screen(экран) получаем границы экрана, а затем и размер экрана в double
        return Screen.getPrimary().getBounds().getWidth();
    }

    private static double initializeMaxHeight() {
        return Screen.getPrimary().getBounds().getHeight();
    }

    //Создадим перечисление со всевозможными положениями курсора относительно окна
    private enum ScreenFrame {
        UPPER_LEFT_CORNER,
        UPPER_RIGHT_CORNER,
        LOWER_LEFT_CORNER,
        LOWER_RIGHT_CORNER,
        UPPER_SIDE,
        LOWER_SIDE,
        LEFT_SIDE,
        RIGHT_SIDE,
        IN_STAGE,
        IN_CONTROLLER
    }

    //Добавляем список классов, которые блокируют перетаскивание
    //Список может хранить любой класс, это указано с помощью Wildcard
    private final List<Class<?>> blockingControls = List.of(
            Button.class,
            TextInputControl.class,
            ComboBox.class
    );

    //Конструктор класса в качестве аргумента принимает Stage с которым будет работать
    public ResizeListener(Stage stage) {
        this.stage = stage;
    }

    //Переопределяем метод handle интерфейса EventHandler, который на вход принимает
    //Экземпляр класса MouseEvent
    @Override
    public void handle(MouseEvent event) {
        //Если у нас окно открыто в полный экран, нам нету смысла изменять его размеры или перемещать
        //А так же это оптимизирует приложение т.к. не обрабатываются всевозможные события мыши, когда окно isFullScreen
        if (!stage.isFullScreen()) {
            //Проверяем, что курсор мыши меняет положение, то есть получаем текущие действие от MouseEvent
            //С помощью метода event.getEventType(), и если это событие перемещение, то выполняем код ниже
            if (MouseEvent.MOUSE_MOVED.equals(event.getEventType())) {
                updateCursor(event);
            }//Проверяем что событие мышки-это нажатие на ней
            else if (MouseEvent.MOUSE_PRESSED.equals(event.getEventType())) {
                storeInitialState(event);
            }//Проверяем, что событие мышки-это её перемещение с нажатой кнопкой
            else if (MouseEvent.MOUSE_DRAGGED.equals(event.getEventType())) {
                handleDrag(event);
            }
        }
    }

    //Метод для определения позиции курсора и изменения его внешнего вида
    private void updateCursor(MouseEvent event) {
        // ПРОВЕРКА: Если курсор над контролером - блокируем изменение размеров/перетаскивание
        if (isCursorOverBlockingControl(event)) {
            stage.getScene().setCursor(Cursor.DEFAULT);
            cursorPosition = ScreenFrame.IN_CONTROLLER;
            return;
        }
        //Метод getX и getY у экземпляра MouseEvent
        //Возвращает горизонтальное положение события относительно источника MouseEvent.
        //Источником события MouseEvent будет выступать Stage
        //Проверяем что что положение курсора находиться в левом верхнем углу
        if (event.getX() < BORDER && event.getY() < BORDER) {
            //Изменяем курсор на двунаправленную стрелочку в сторону северо-запада
            stage.getScene().setCursor(Cursor.NW_RESIZE);
            //Так как мы изменяем размер окна в верхнем левом углу, который в javaFX
            //Отвечает за расположения окна на экрана, то изменяя размеры окна в этом углу
            //Фактически нам придётся перемещать его и дорисовывать остальное
            //Что бы сохранить противоположный угол неподвижным
            cursorPosition = ScreenFrame.UPPER_LEFT_CORNER;
        }//Следующим действием проверяем, что наш курсор находиться в нижнем-левом углу
        else if (event.getX() < BORDER && event.getY() > stage.getHeight() - BORDER) {
            stage.getScene().setCursor(Cursor.SW_RESIZE);
            cursorPosition = ScreenFrame.LOWER_LEFT_CORNER;
        }//Курсор в правом верхнем углу
        else if (event.getX() > stage.getWidth() - BORDER && event.getY() < BORDER) {
            stage.getScene().setCursor(Cursor.NE_RESIZE);
            cursorPosition = ScreenFrame.UPPER_RIGHT_CORNER;
        }//Правый нижний угол(самый плавный, так не меняется положение окна)
        else if (event.getX() > stage.getWidth() - BORDER && event.getY() > stage.getHeight() - BORDER) {
            stage.getScene().setCursor(Cursor.SE_RESIZE);
            cursorPosition = ScreenFrame.LOWER_RIGHT_CORNER;
        }//Курсор на левой или правой стороне окна
        else if (event.getX() < BORDER || event.getX() > stage.getWidth() - BORDER) {
            stage.getScene().setCursor(Cursor.E_RESIZE);
            cursorPosition = (event.getX() < BORDER) ? ScreenFrame.LEFT_SIDE : ScreenFrame.RIGHT_SIDE;
        }//Курсор на верхней или нижней стороне окна
        else if (event.getY() < BORDER || event.getY() > stage.getHeight() - BORDER) {
            stage.getScene().setCursor(Cursor.N_RESIZE);
            cursorPosition = (event.getY() < BORDER) ? ScreenFrame.UPPER_SIDE : ScreenFrame.LOWER_SIDE;
        } //Когда курсор внутри окна(не касается BORDER) и когда за ним, запрещаем изменения размеров, курсор стандартный
        else {
            stage.getScene().setCursor(Cursor.DEFAULT);
            cursorPosition = ScreenFrame.IN_STAGE;
        }
    }

    //Метод для сохранения исходного состояния окна при нажатии мыши
    private void storeInitialState(MouseEvent event) {
        //Запишем положение окна
        initialX = stage.getX();
        initialY = stage.getY();
        //Запишем исходные размеры окна
        initialWidth = stage.getWidth();
        initialHeight = stage.getHeight();
        //Запишем положение курсора в окне
        mouseClickX = event.getX();
        mouseClickY = event.getY();
    }

    //Метод для обработки перемещения мыши с зажатой кнопкой (изменение размеров или перемещение окна)
    private void handleDrag(MouseEvent event) {
        //Вычисляем разницу между текущим положением курсора и исходным положением окна
        double deltaX = event.getScreenX() - initialX;
        double deltaY = event.getScreenY() - initialY;

        //Инициализируем переменные для новых размеров и позиции
        double newWidth = initialWidth;
        double newHeight = initialHeight;
        double newX = initialX;
        double newY = initialY;

        //В зависимости от позиции курсора вычисляем новые параметры окна
        switch (cursorPosition) {
            case UPPER_LEFT_CORNER:
                //Ширина будет иметь формулу: исходная ширина - разница по X
                newWidth = Math.max(MIN_WIDTH, initialWidth - deltaX);
                //Высота будет иметь формулу: исходная высота - разница по Y
                newHeight = Math.max(MIN_HEIGHT, initialHeight - deltaY);
                //Перемещаем окно так, чтобы противоположный угол оставался на месте
                newX = initialX + (initialWidth - newWidth);
                newY = initialY + (initialHeight - newHeight);
                break;
            case LOWER_LEFT_CORNER:
                //Перерисовываем положение + меняем размер
                newWidth = Math.max(MIN_WIDTH, initialWidth - deltaX);
                newHeight = Math.max(MIN_HEIGHT, deltaY);
                newX = initialX + (initialWidth - newWidth);
                //Y остается без изменений, так как тянем снизу
                break;
            case UPPER_RIGHT_CORNER:
                //Меняем размер без изменения положения по X
                newWidth = Math.max(MIN_WIDTH, deltaX);
                newHeight = Math.max(MIN_HEIGHT, initialHeight - deltaY);
                newY = initialY + (initialHeight - newHeight);
                break;
            case LOWER_RIGHT_CORNER:
                //Правый нижний угол - самый простой случай, меняем только размеры
                newWidth = Math.max(MIN_WIDTH, deltaX);
                newHeight = Math.max(MIN_HEIGHT, deltaY);
                //Положение окна не меняется
                break;
            case LEFT_SIDE:
                //Изменяем только ширину с левой стороны
                newWidth = Math.max(MIN_WIDTH, initialWidth - deltaX);
                newX = initialX + (initialWidth - newWidth);
                break;
            case RIGHT_SIDE:
                //Изменяем только ширину с правой стороны
                newWidth = Math.max(MIN_WIDTH, deltaX);
                break;
            case UPPER_SIDE:
                //Изменяем только высоту с верхней стороны
                newHeight = Math.max(MIN_HEIGHT, initialHeight - deltaY);
                newY = initialY + (initialHeight - newHeight);
                break;
            case LOWER_SIDE:
                //Изменяем только высоту с нижней стороны
                newHeight = Math.max(MIN_HEIGHT, deltaY);
                break;
            case IN_STAGE:
                //Перемещаем окно без изменения размеров
                newX = event.getScreenX() - mouseClickX;
                newY = event.getScreenY() - mouseClickY;
                break;
        }

        //Применяем изменения, если они в пределах допустимых размеров
        if (newWidth <= MAX_WIDTH && newHeight <= MAX_HEIGHT) {
            stage.setWidth(newWidth);
            stage.setHeight(newHeight);
            stage.setX(newX);
            stage.setY(newY);
        }
    }

    //Метод проверки, находится ли курсор над блокирующим контролером
    private boolean isCursorOverBlockingControl(MouseEvent event) {
        //Метод getTarget возвращает object, над которым происходит событие мыши, явно приведём его к классу Node
        Node target = (Node) event.getTarget();
        return isBlockingControlOrParent(target);
    }

    //Рекурсивная проверка самого элемента и всех его родителей
    private boolean isBlockingControlOrParent(Node node) {
        //Проверяем что объект, переданный в качестве аргумента не пустой
        if (node == null) {
            return false;
        }

        //Проверяем текущий узел, с помощью stream() преобразовываем список в поток
        boolean isBlocking = blockingControls.stream()
                //Метод anyMatch проверяет, соответствует ли хотя бы один элемент потока условию.
                //В лямбда-функции проверяется, объект, переданный в качестве параметра
                //Является классом или его наследником из списка blockingControls
                .anyMatch(controlClass -> controlClass.isInstance(node));

        if (isBlocking) {
            return true; //Нашли блокирующий контролер
        }

        //Рекурсивно проверяем родителя, то есть метод вызывает сам себя, пока не найдёт true или пустоту
        //Это важно, потому что в TextInputControl и некоторых стилизованных кнопках и т.д.
        //Сам элемент кнопка или TextInputControl может перекрываться текстом, иконкой и т.д.
        //Это приведёт к неправильной работе метода, потому что обработчик события будет к примеру на тексте
        return isBlockingControlOrParent(node.getParent());
    }
}