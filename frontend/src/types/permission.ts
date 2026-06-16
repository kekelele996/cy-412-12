import type { UserRole } from '../constants/user';

export type PermissionCode =
  | 'dashboard:view'
  | 'repair:view'
  | 'repair:create'
  | 'repair:assign'
  | 'repair:update'
  | 'payment:view'
  | 'payment:pay'
  | 'announcement:view'
  | 'announcement:publish'
  | 'user:profile'
  | 'operationLog:view'
  | 'facility:view'
  | 'facility:book'
  | 'facility:manage'
  | 'facility:slotManage'
  | 'facility:bookingView';

export interface RoutePermissionMeta {
  permission: PermissionCode;
  roles?: UserRole[];
}
