import type { CSSProperties } from 'react'
import type { AvatarKey } from '../auth/types'
import { getAvatarOption } from './avatarOptions'
import './avatar.css'

interface UserAvatarProps {
    avatarKey: AvatarKey
    avatarColor: string
    size?: 'small' | 'medium' | 'large'
    className?: string
}

export function UserAvatar({
                               avatarKey,
                               avatarColor,
                               size = 'medium',
                               className = '',
                           }: UserAvatarProps) {
    const avatar = getAvatarOption(avatarKey)

    const imageStyle = {
        '--avatar-image-scale': avatar.scale ?? 0.88,
    } as CSSProperties

    return (
        <span
            className={`user-avatar user-avatar-${size} ${className}`}
            style={{ backgroundColor: avatarColor }}
            aria-label={`${avatar.label} avatar`}
            role="img"
        >
            <img
                alt=""
                src={avatar.image}
                style={imageStyle}
            />
        </span>
    )
}