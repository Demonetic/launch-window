import alien from '../../assets/avatars/alien.png'
import astronaut from '../../assets/avatars/astronaut.png'
import lunarRover from '../../assets/avatars/lunar-rover.png'
import moonBaseRobot from '../../assets/avatars/moon-base-robot.png'
import planet from '../../assets/avatars/planet.png'
import rocket from '../../assets/avatars/rocket.png'
import satellite from '../../assets/avatars/satellite.png'
import telescope from '../../assets/avatars/telescope.png'
import type { AvatarKey } from '../auth/types'

export interface AvatarOption {
    key: AvatarKey
    label: string
    image: string
    scale?: number
}

export const avatarOptions: AvatarOption[] = [
    {
        key: 'ASTRONAUT',
        label: 'Astronaut',
        image: astronaut,
    },
    {
        key: 'ALIEN',
        label: 'Alien',
        image: alien,
    },
    {
        key: 'MOON_BASE_ROBOT',
        label: 'Moon base robot',
        image: moonBaseRobot,
        scale: 0.9,
    },
    {
        key: 'ROCKET',
        label: 'Rocket',
        image: rocket,
        scale: 0.82,
    },
    {
        key: 'SATELLITE',
        label: 'Satellite',
        image: satellite,
        scale: 0.78,
    },
    {
        key: 'PLANET',
        label: 'Planet',
        image: planet,
        scale: 0.84,
    },
    {
        key: 'LUNAR_ROVER',
        label: 'Lunar rover',
        image: lunarRover,
        scale: 0.86,
    },
    {
        key: 'TELESCOPE',
        label: 'Telescope',
        image: telescope,
        scale: 0.8,
    },
]

export const avatarColors = [
    '#FFFFFF',
    '#B8C1FF',
    '#D8B4FE',
    '#9DDDF5',
    '#9FE0C0',
    '#FFD39A',
    '#FFB5C0',
    '#252B42',
] as const

export function getAvatarOption(
    avatarKey: AvatarKey,
): AvatarOption {
    return (
        avatarOptions.find(
            (option) => option.key === avatarKey,
        ) ?? avatarOptions[0]
    )
}