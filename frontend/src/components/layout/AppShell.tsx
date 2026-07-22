import { DesktopNavigation } from './DesktopNavigation'
import { MobileNavigation } from './MobileNavigation'
import {Outlet, ScrollRestoration,} from 'react-router'
import './layout.css'

export function AppShell() {
    return (
        <div className="app-layout">
            <DesktopNavigation />

            <div className="app-content">
                <Outlet />
            </div>

            <MobileNavigation />
            <ScrollRestoration />
        </div>
    )
}