package org.niikage.planr.features.eventparticipants.service

import org.niikage.planr.features.eventparticipants.query.EventParticipantsList
import org.niikage.planr.features.events.domain.EventId
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.kernel.PageRequest
import org.niikage.planr.shared.exceptions.NotFoundException
import org.niikage.planr.shared.exceptions.ConflictException

interface EventParticipantService {
    /**
     * Получает список участников события.
     *
     * Возвращает список участников с учетом параметров постраничного отображения.
     *
     * @param eventId Уникальный идентификатор события.
     * @param pageRequest Параметры постраничного отображения (номер страницы, размер страницы).
     * @return Объект списка участников события с информацией о каждом участнике.
     * @throws NotFoundException если событие с указанным ID не существует.
     */
    suspend fun getEventParticipants(
        eventId: EventId,
        pageRequest: PageRequest
    ): EventParticipantsList

    /**
     * Добавляет создателя события в качестве участника.
     *
     * Автоматически добавляет указанного пользователя как создателя события.
     *
     * @param eventId Уникальный идентификатор события.
     * @param userId Уникальный идентификатор пользователя, который будет добавлен как создатель.
     * @throws NotFoundException если событие или пользователь не существует.
     * @throws ConflictException если пользователь уже является участником события.
     */
    suspend fun addEventCreator(
        eventId: EventId,
        userId: UserId
    )

    /**
     * Приглашает пользователей на событие.
     *
     * Отправляет Именные приглашения указанным пользователям на участие в событии.
     *
     * @param eventId Уникальный идентификатор события.
     * @param userIds Список идентификаторов пользователей для приглашения.
     * @return Список ID пользователей, которых удалось пригласить.
     * @throws NotFoundException если событие не существует.
     */
    suspend fun inviteParticipants(
        eventId: EventId,
        userIds: List<UserId>
    ): List<UserId>

    /**
     * Добавляет одного участника на событие.
     *
     * Регистрирует указанного пользователя как участника события.
     *
     * @param eventId Уникальный идентификатор события.
     * @param userId Уникальный идентификатор пользователя для добавления.
     * @return Идентификатор добавленного пользователя.
     * @throws NotFoundException если событие или пользователь не существует.
     * @throws ConflictException если пользователь уже является участником события.
     */
    suspend fun addParticipant(
        eventId: EventId,
        userId: UserId
    ): UserId

    /**
     * Добавляет нескольких участников на событие.
     *
     * Регистрирует указанных пользователей как участников события.
     *
     * @param eventId Уникальный идентификатор события.
     * @param userIds Список идентификаторов пользователей для добавления.
     * @return Список ID успешно добавленных пользователей.
     * @throws NotFoundException если событие не существует.
     */
    suspend fun addParticipants(
        eventId: EventId,
        userIds: List<UserId>
    ): List<UserId>

    /**
     * Удаляет участника из события.
     *
     * Удаляет указанного пользователя из списка участников события.
     *
     * @param eventId Уникальный идентификатор события.
     * @param userId Уникальный идентификатор пользователя для удаления.
     * @throws NotFoundException если событие, пользователь или участие не существует.
     */
    suspend fun removeParticipant(
        eventId: EventId,
        userId: UserId
    )

    /**
     * Удаляет нескольких участников из события.
     *
     * Удаляет указанных пользователей из списка участников события.
     *
     * @param eventId Уникальный идентификатор события.
     * @param userIds Список идентификаторов пользователей для удаления.
     * @return Количество успешно удаленных участников.
     * @throws NotFoundException если событие не существует.
     */
    suspend fun removeParticipants(
        eventId: EventId,
        userIds: List<UserId>
    ): Int
}