package org.niikage.planr.features.recentcontacts.service

import org.niikage.planr.features.recentcontacts.domain.RecentContactsList
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.kernel.PageRequest

interface RecentContactService {
    /**
     * Получает список недавних контактов пользователя.
     *
     * Возвращает контакты с учетом параметров постраничного отображения.
     * Если владелец контактов не существует, возвращает пустой список.
     *
     * @param ownerId Уникальный идентификатор владельца контактов.
     * @param pageRequest Параметры постраничного отображения (номер страницы, размер страницы).
     * @return Объект списка недавних контактов для указанного пользователя или пустой список.
     */
    suspend fun findAllByOwnerId(ownerId: UserId, pageRequest: PageRequest): RecentContactsList

    /**
     * Добавляет контакт в список недавних или обновляет время последнего контакта.
     *
     * Если контакт уже существует, обновляется временная метка последнего взаимодействия.
     * Если контакта нет, он добавляется как новый.
     * Проверка существования владельца не выполняется.
     *
     * @param ownerId Уникальный идентификатор владельца контактов.
     * @param userId Уникальный идентификатор добавляемого или обновляемого контакта.
     */
    suspend fun addOrRefresh(ownerId: UserId, userId: UserId)

    /**
     * Удаляет конкретный контакт из списка недавних.
     *
     * Если контакта не существует, операция игнорируется без ошибок.
     *
     * @param ownerId Уникальный идентификатор владельца контактов.
     * @param userId Уникальный идентификатор удаляемого контакта.
     */
    suspend fun delete(ownerId: UserId, userId: UserId)

    /**
     * Удаляет все истекшие контакты для пользователя.
     *
     * Удаляет контакты, время последнего взаимодействия с которыми превысило установленный срок.
     *
     * @param ownerId Уникальный идентификатор владельца контактов.
     */
    suspend fun deleteExpiredByOwnerId(ownerId: UserId)

    /**
     * Удаляет все контакты пользователя.
     *
     * Полностью очищает список недавних контактов для указанного пользователя.
     *
     * @param ownerId Уникальный идентификатор владельца контактов.
     */
    suspend fun deleteAllByOwnerId(ownerId: UserId)
}