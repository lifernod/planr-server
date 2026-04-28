package org.niikage.planr.features.invitations.service

import org.niikage.planr.features.invitations.domain.Invitation
import org.niikage.planr.features.invitations.domain.InvitationResponseStatus
import org.niikage.planr.features.invitations.domain.NamedInvitation
import org.niikage.planr.features.invitations.domain.UnnamedInvitation
import org.niikage.planr.features.users.domain.UserId
import org.niikage.planr.shared.exceptions.NotFoundException
import org.niikage.planr.shared.exceptions.ConflictException
import org.niikage.planr.shared.exceptions.UnauthorizedException
import java.util.*

interface InvitationService {
    /**
     * Получает приглашение по его уникальному идентификатору.
     *
     * @param invitationId Уникальный идентификатор приглашения.
     * @return Объект приглашения с полной информацией.
     * @throws NotFoundException если приглашение с указанным ID не существует.
     */
    suspend fun getInvitation(invitationId: UUID): Invitation

    /**
     * Создает безымянное приглашение на событие.
     *
     * Безымянное приглашение - это уникальное и одноразовое приглашение на событие,
     * которое может быть использовано любым пользователем без проверки получателя.
     * Приглашение не привязано к конкретному пользователю.
     *
     * @param invitation Объект безымянного приглашения с информацией о событии и целях.
     * @return Уникальный идентификатор созданного приглашения.
     */
    suspend fun createUnnamedInvitation(invitation: UnnamedInvitation): UUID

    /**
     * Отправляет именованные приглашения пользователям.
     *
     * Именованные приглашения адресованы конкретным пользователям и могут быть
     * приняты или отклонены только адресатом. Каждое приглашение привязано к
     * определенному пользователю и событию.
     *
     * @param invitations Список именованных приглашений для отправки.
     */
    suspend fun sendInvitations(invitations: List<NamedInvitation>)

    /**
     * Обрабатывает ответ на приглашение.
     *
     * Позволяет пользователю принять или отклонить приглашение (как именованное, так и безымянное).
     * После обработки статуса приглашения вызывается callback функция для выполнения дополнительных
     * действий (например, добавление участника к событию). Callback должен вернуть true, если
     * операция успешна, иначе false.
     *
     * @param invitationId Уникальный идентификатор приглашения для обработки.
     * @param respondentId Уникальный идентификатор пользователя, отвечающего на приглашение.
     * @param status Статус ответа (по умолчанию ACCEPTED - принято).
     * @param callback Функция обратного вызова, которая вызывается после обновления статуса приглашения.
     * @return true если приглашение успешно обработано, false если callback вернул false.
     * @throws NotFoundException если приглашение не найдено.
     * @throws ConflictException если приглашение уже было отвечено ранее (статус != PENDING).
     * @throws UnauthorizedException если это именованное приглашение и receiver.id != respondentId.
     */
    suspend fun answerInvitation(
        invitationId: UUID,
        respondentId: UserId,
        status: InvitationResponseStatus = InvitationResponseStatus.ACCEPTED,
        callback: suspend (invitation: Invitation) -> Boolean
    ): Boolean
}