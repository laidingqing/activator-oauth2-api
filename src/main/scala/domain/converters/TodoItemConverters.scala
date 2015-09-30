package domain.converters

import domain.models.{TodoItemDto, TodoItem}

object TodoItemConverters {
  def toTodoItemDto(todoItem: TodoItem): TodoItemDto = TodoItemDto(todoItem.title, todoItem.description)

}