import {
    CalendarDays,
    House,
    MessageCircle,
    NotebookPen,
    type LucideIcon,
    UsersRound,
} from 'lucide-react'

export interface NavigationItem {
    to: string
    label: string
    icon: LucideIcon
    end?: boolean
}

export const navigationItems: NavigationItem[] = [
    {
        to: '/',
        label: 'Launches',
        icon: House,
        end: true,
    },
    {
        to: '/calendar',
        label: 'Calendar',
        icon: CalendarDays,
    },
    {
        to: '/notes',
        label: 'Notes',
        icon: NotebookPen,
    },
    {
        to: '/friends',
        label: 'Friends',
        icon: UsersRound,
    },
    {
        to: '/messages',
        label: 'Messages',
        icon: MessageCircle,
    },
]