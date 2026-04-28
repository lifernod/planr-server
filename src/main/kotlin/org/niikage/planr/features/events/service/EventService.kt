package org.niikage.planr.features.events.service

import org.niikage.planr.features.events.domain.EventDomain
import org.niikage.planr.features.events.domain.EventId
import org.niikage.planr.features.events.dto.EventCreateRequest
import org.niikage.planr.features.events.dto.EventUpdateRequest
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.kernel.PageRequest
import org.niikage.planr.shared.exceptions.NotFoundException
import org.niikage.planr.shared.exceptions.UnauthorizedException

interface EventService {
    /**
     * Получает событие по его уникальному идентификатору.
     *
     * @param id Уникальный идентификатор события.
     * @return Объект домена события, если найден.
     * @throws NotFoundException если событие с указанным ID не существует.
     */
    suspend fun getEvent(id: EventId): EventDomain

    /**
     * Получает список событий, созданных пользователем.
     *
     * Возвращает события в порядке с учетом параметров постраничного отображения.
     *
     * @param creatorId Уникальный идентификатор создателя событий.
     * @param pageRequest Параметры постраничного отображения (номер страницы, размер страницы).
     * @return Список объектов домена событий, созданных указанным пользователем.
     */
    suspend fun getCreatedEvents(creatorId: UserId, pageRequest: PageRequest): List<EventDomain>

    /**
     * Получает список событий, в которых участвует пользователь.
     *
     * Возвращает события, в которых пользователь зарегистрирован как участник.
     *
     * @param userId Уникальный идентификатор пользователя.
     * @param pageRequest Параметры постраничного отображения (номер страницы, размер страницы).
     * @return Список объектов домена событий, в которых участвует указанный пользователь.
     */
    suspend fun getParticipatedEvents(userId: UserId, pageRequest: PageRequest): List<EventDomain>

    /**
     * Создает новое событие.
     *
     * Новому событию присваивается случайный уникальный ID.
     * Время создания автоматически устанавливается на текущее время.
     * Создатель события автоматически добавляется в список участников.
     *
     * @param creatorId Уникальный идентификатор создателя события.
     * @param request Запрос на создание события с информацией о названии, описании и других деталях.
     * @return Недавно созданный объект домена события.
     * @throws NotFoundException если пользователь-создатель не существует.
     */
    suspend fun create(creatorId: UserId, request: EventCreateRequest): EventDomain

    /**
     * Обновляет существующее событие.
     *
     * Только создатель события может обновлять его. Null значения в запросе означают "без изменений".
     *
     * @param id Уникальный идентификатор события для обновления.
     * @param requestFromUser Уникальный идентификатор пользователя, запрашивающего обновление.
     * @param request Запрос на обновление события с дополнительной информацией.
     * @return Обновленный объект домена события.
     * @throws NotFoundException если событие с указанным ID не существует.
     * @throws UnauthorizedException если пользователь не является создателем события.
     */
    suspend fun update(
        id: EventId,
        requestFromUser: UserId,
        request: EventUpdateRequest): EventDomain

    /**
     * Удаляет событие по его уникальному идентификатору.
     *
     * Удаляет событие и все связанные с ним данные из системы.
     *
     * @param id Уникальный идентификатор события для удаления.
     */
    suspend fun delete(id: EventId)
}