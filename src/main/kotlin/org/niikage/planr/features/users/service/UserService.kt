package org.niikage.planr.features.users.service

import org.niikage.planr.features.users.domain.UserDomain
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.features.users.domain.UserSocials
import org.niikage.planr.features.users.dto.UserCreateRequest
import org.niikage.planr.features.users.dto.UserUpdateRequest
import org.niikage.planr.shared.exceptions.BadRequestException
import org.niikage.planr.shared.exceptions.ConflictException
import org.niikage.planr.shared.exceptions.NotFoundException

interface UserService {
    /**
     * Получает пользователя по его уникальному идентификатору.
     *
     * @param id Уникальный идентификатор пользователя для получения.
     * @return Объект домена пользователя, если найден.
     * @throws NotFoundException если пользователь с указанным ID не существует.
     */
    suspend fun getUser(id: UserId): UserDomain

    /**
     * Получает пользователя по его социальным сетям.
     *
     * Ищет пользователя по ID Telegram или VK.
     * Метод отдает приоритет Telegram, если доступен, иначе использует VK.
     *
     * @param socials Объект социальных сетей пользователя с идентификаторами.
     * @return Объект домена пользователя, если найден.
     * @throws NotFoundException если пользователь с указанными социальными сетями не найден.
     */
    suspend fun getUser(socials: UserSocials): UserDomain

    /**
     * Получает список пользователей по их идентификаторам.
     *
     * @param userIds Список ID пользователей для получения.
     * @return Список объектов домена пользователей. Возвращает пустой список, если пользователи не найдены.
     */
    suspend fun getUsers(userIds: List<UserId>): List<UserDomain>

    /**
     * Создает нового пользователя с предоставленной информацией.
     *
     * Проверяет, что указана хотя бы одна социальная сеть (Telegram или VK).
     * Новому пользователю присваивается случайный уникальный ID и роль USER по умолчанию.
     * Временная метка создания автоматически устанавливается на текущее время.
     *
     * @param request Запрос на создание пользователя с именем и информацией о социальных сетях.
     * @return Недавно созданный объект домена пользователя.
     * @throws BadRequestException если не указана ни одна социальная сеть (Telegram или VK).
     * @throws ConflictException если пользователь с теми же социальными сетями уже существует.
     */
    suspend fun create(request: UserCreateRequest): UserDomain

    /**
     * Обновляет существующего пользователя с предоставленной информацией.
     *
     * Обновляются только поля, указанные в запросе. Null значения в запросе означают "без изменений".
     * Пользователь должен существовать перед обновлением. Социальные сети объединяются с существующими,
     * сохраняя любые существующие соединения, не включенные в запрос на обновление.
     *
     * @param id Уникальный идентификатор пользователя для обновления.
     * @param request Запрос на обновление пользователя с дополнительным именем и информацией о социальных сетях.
     * @return Обновленный объект домена пользователя.
     * @throws NotFoundException если пользователь с указанным ID не существует.
     * @throws ConflictException если обновление создаст дублированного пользователя с теми же социальными сетями.
     */
    suspend fun update(id: UserId, request: UserUpdateRequest): UserDomain

    /**
     * Удаляет пользователя по его уникальному идентификатору.
     *
     * Удаляет пользователя из системы окончательно. Если пользователь не найден, то игнорирует запрос, не создавая ошибки.
     *
     * @param id Уникальный идентификатор пользователя для удаления.
     */
    suspend fun delete(id: UserId)
}