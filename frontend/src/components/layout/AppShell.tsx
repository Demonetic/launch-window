import { Outlet } from 'react-router'
import { DesktopNavigation } from './DesktopNavigation'
import { MobileNavigation } from './MobileNavigation'
import './layout.css'

export function AppShell() {
    return (
        <div className="app-layout">
            <DesktopNavigation />

            <div className="app-content">
                <Outlet />
            </div>

            <MobileNavigation />
        </div>
    )
}