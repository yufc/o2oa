package com.x.meeting.assemble.control.jaxrs.room;

import com.google.gson.JsonElement;
import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.entity.annotation.CheckPersistType;
import com.x.base.core.project.bean.WrapCopier;
import com.x.base.core.project.bean.WrapCopierFactory;
import com.x.base.core.project.exception.ExceptionAccessDenied;
import com.x.base.core.project.exception.ExceptionEntityNotExist;
import com.x.base.core.project.exception.ExceptionWhen;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.http.WrapOutId;
import com.x.base.core.project.jaxrs.WoId;
import com.x.base.core.project.tools.ListTools;
import com.x.meeting.assemble.control.Business;
import com.x.meeting.assemble.control.WrapTools;
import com.x.meeting.assemble.control.jaxrs.room.ActionCreate.Wi;
import com.x.meeting.core.entity.Room;

class ActionEdit extends BaseAction {

	ActionResult<Wo> execute(EffectivePerson effectivePerson, String id, JsonElement jsonElement) throws Exception {
		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			ActionResult<Wo> result = new ActionResult<>();
			Business business = new Business(emc);
			Wi wi = this.convertToWrapIn(jsonElement, Wi.class);
			Room room = emc.find(id, Room.class);
			if (null == room) {
				throw new ExceptionEntityNotExist(id, Room.class);
			}
			if (!business.roomEditAvailable(effectivePerson, room)) {
				throw new ExceptionAccessDenied(effectivePerson);
			}
			emc.beginTransaction(Room.class);
			Wi.copier.copy(wi, room);
			/* 检查审核人 */
			this.checkAuditor(business, room);
			emc.check(room, CheckPersistType.all);
			emc.commit();
			Wo wo = new Wo();
			wo.setId(room.getId());
			result.setData(wo);
			return result;
		}
	}

	public static class Wi extends Room {

		private static final long serialVersionUID = -2110046277122057197L;

		static WrapCopier<Wi, Room> copier = WrapCopierFactory.wi(Wi.class, Room.class, null,
				ListTools.toList(JpaObject.FieldsUnmodify));

	}

	public static class Wo extends WoId {

	}

}
